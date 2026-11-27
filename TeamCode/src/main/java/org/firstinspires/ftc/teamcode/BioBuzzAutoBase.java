package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/** Shared strategy and motion controller for the four BIOBUZZ starting positions. */
public abstract class BioBuzzAutoBase extends LinearOpMode {
    // Baseline: 537.7-tick motor encoder with 96 mm mecanum wheels.
    // Update the correction factors after the 24-inch physical calibration tests.
    private static final double MOTOR_TICKS_PER_REV = 537.7;
    private static final double WHEEL_DIAMETER_INCHES = 96.0 / 25.4;
    private static final double FORWARD_CORRECTION = 1.00;
    private static final double STRAFE_CORRECTION = 1.12;
    private static final double FORWARD_TICKS_PER_INCH =
            MOTOR_TICKS_PER_REV / (Math.PI * WHEEL_DIAMETER_INCHES) * FORWARD_CORRECTION;
    private static final double STRAFE_TICKS_PER_INCH =
            MOTOR_TICKS_PER_REV / (Math.PI * WHEEL_DIAMETER_INCHES) * STRAFE_CORRECTION;

    private static final double DRIVE_POWER = 0.30;
    private static final double LEAVE_DISTANCE_INCHES = 18.0;
    private static final double LOADING_ZONE_STRAFE_INCHES = 8.0;

    private static final double AUTO_CUTOFF_SECONDS = 29.0;
    private static final double STEP_TIMEOUT_SECONDS = 6.0;
    private static final double STALL_TIMEOUT_SECONDS = 1.25;
    private static final int TARGET_TOLERANCE_TICKS = 20;
    private static final int PROGRESS_TICKS = 5;
    private static final double HEADING_KP = 0.018;
    private static final double TURN_KP = 0.018;
    private static final double MIN_TURN_POWER = 0.12;
    private static final double MAX_TURN_POWER = 0.32;
    private static final double AIM_TOLERANCE_DEGREES = 1.5;
    private static final double CAMERA_TO_LAUNCHER_OFFSET_DEGREES = 0.0;
    private static final double AIM_TIMEOUT_SECONDS = 2.5;

    private final boolean finishInLoadingZone;
    private final boolean redAlliance;
    private final String startingPosition;
    private final DcMotor[] motors = new DcMotor[4];
    private final ElapsedTime matchClock = new ElapsedTime();
    private BioBuzzMechanisms mechanisms;
    private IMU imu;
    private Camera camera;
    private String outcome = "READY";

    protected BioBuzzAutoBase(boolean finishInLoadingZone, boolean redAlliance,
                              String startingPosition) {
        this.finishInLoadingZone = finishInLoadingZone;
        this.redAlliance = redAlliance;
        this.startingPosition = startingPosition;
    }

    @Override
    public final void runOpMode() throws InterruptedException {
        try {
            initializeHardware();
            while (opModeInInit()) {
                telemetry.addData("BIOBUZZ AUTO", startingPosition);
                telemetry.addData("Strategy", finishInLoadingZone
                        ? "LEAVE -> PARK" : "LEAVE -> HOLD");
                telemetry.addData("Drive mode", "ENCODER + IMU HEADING");
                telemetry.addData("HIVE scoring", "Disabled: drive-only Auto");
                telemetry.addLine("Back touches wall; front faces field center.");
                if (finishInLoadingZone) {
                    telemetry.addLine("Start beside YOUR loading zone with zone on robot RIGHT.");
                } else {
                    telemetry.addLine("Start on garden-side wall, clear of HIVE and partner.");
                }
                telemetry.update();
                sleep(20);
            }

            waitForStart();
            if (isStopRequested()) return;
            matchClock.reset();

            if (!moveForwardForLeave()) return;

            // Shooter hardware is not installed. Keep the existing scoring hooks for later.
            // if (aimAtHive()) mechanisms.scoreFourPreloads(this);
            if (!opModeIsActive()) return;

            if (finishInLoadingZone) {
                if (!moveRightForPark()) return;
                outcome = "LEAVE + PARK route complete";
            } else {
                outcome = "LEAVE route complete; holding position";
            }
        } finally {
            stopAll();
            if (camera != null) camera.close();
            telemetry.addData("BIOBUZZ AUTO", outcome);
            telemetry.update();
        }
    }

    private void initializeHardware() {
        String[] names = {"frontLeftMotor", "backLeftMotor", "frontRightMotor", "backRightMotor"};
        for (int i = 0; i < motors.length; i++) {
            motors[i] = hardwareMap.get(DcMotor.class, names[i]);
            motors[i].setPower(0);
            motors[i].setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            motors[i].setDirection(i < 2
                    ? DcMotorSimple.Direction.FORWARD : DcMotorSimple.Direction.REVERSE);
            motors[i].setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motors[i].setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD)));
        imu.resetYaw();
        // camera = new Camera(hardwareMap);
        // mechanisms = new BioBuzzMechanisms(hardwareMap);
    }

    private boolean moveForwardForLeave() throws InterruptedException {
        return moveByEncoder(LEAVE_DISTANCE_INCHES, 0, "LEAVE");
    }

    private boolean moveRightForPark() throws InterruptedException {
        return moveByEncoder(0, LOADING_ZONE_STRAFE_INCHES, "PARK");
    }

    /** Positive forward or right; each wheel receives its own relative encoder target. */
    private boolean moveByEncoder(double forwardInches, double rightInches, String step)
            throws InterruptedException {
        if (!canContinue()) return false;
        int forwardTicks = (int) Math.round(forwardInches * FORWARD_TICKS_PER_INCH);
        int rightTicks = (int) Math.round(rightInches * STRAFE_TICKS_PER_INCH);
        int[] deltas = {forwardTicks + rightTicks, forwardTicks - rightTicks,
                forwardTicks - rightTicks, forwardTicks + rightTicks};
        int[] targets = new int[4];
        int[] lastProgress = new int[4];
        double[] progressTime = new double[4];
        ElapsedTime stepClock = new ElapsedTime();
        double targetHeading = yawDegrees();

        for (int i = 0; i < motors.length; i++) {
            int current = motors[i].getCurrentPosition();
            targets[i] = current + deltas[i];
            lastProgress[i] = current;
            motors[i].setTargetPosition(targets[i]);
            motors[i].setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motors[i].setPower(DRIVE_POWER);
        }

        while (canContinue() && stepClock.seconds() < STEP_TIMEOUT_SECONDS) {
            boolean allFinished = true;
            for (int i = 0; i < motors.length; i++) {
                int current = motors[i].getCurrentPosition();
                if (Math.abs(targets[i] - current) <= TARGET_TOLERANCE_TICKS) continue;
                allFinished = false;
                if (Math.abs(current - lastProgress[i]) >= PROGRESS_TICKS) {
                    lastProgress[i] = current;
                    progressTime[i] = stepClock.seconds();
                } else if (stepClock.seconds() - progressTime[i] >= STALL_TIMEOUT_SECONDS) {
                    outcome = step + " stopped: wheel " + (i + 1) + " did not move";
                    stopDrive();
                    return false;
                }
            }
            if (allFinished) {
                stopDrive();
                outcome = step + " complete";
                return true;
            }
            double correction = clamp(normalizeDegrees(targetHeading - yawDegrees()) * HEADING_KP,
                    -0.12, 0.12);
            setRunToPositionPowers(DRIVE_POWER, correction);
            reportStep(step, "encoder");
            sleep(10);
        }
        outcome = step + " stopped: time limit or STOP";
        stopDrive();
        return false;
    }

    /** Centers the alliance HIVE AprilTags; holds the initial IMU heading if vision is unavailable. */
    private boolean aimAtHive() throws InterruptedException {
        int[] targetIds = redAlliance
                ? new int[]{30, 31, 32, 33, 34, 35, 36, 37}
                : new int[]{38, 39, 40, 41, 42, 43, 44, 45};
        ElapsedTime aimClock = new ElapsedTime();
        int stableReadings = 0;
        boolean sawTarget = false;

        while (canContinue() && aimClock.seconds() < AIM_TIMEOUT_SECONDS) {
            double bearing = camera.bestBearingDegrees(targetIds);
            if (Double.isNaN(bearing)) {
                stopDrive();
                sleep(20);
                continue;
            }

            sawTarget = true;
            double error = normalizeDegrees(bearing - CAMERA_TO_LAUNCHER_OFFSET_DEGREES);
            telemetry.addData("AIM", "AprilTag");
            telemetry.addData("Bearing error", "%.1f deg", error);
            telemetry.update();

            if (Math.abs(error) <= AIM_TOLERANCE_DEGREES) {
                stopDrive();
                if (++stableReadings >= 3) {
                    setDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    outcome = "HIVE aim locked";
                    return true;
                }
            } else {
                stableReadings = 0;
                turnWithError(error);
            }
            sleep(20);
        }

        stopDrive();
        setDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if (!canContinue()) return false;
        if (sawTarget) {
            outcome = "HIVE aim stopped: target did not settle";
            return false;
        }

        telemetry.addLine("No HIVE tag found; holding IMU heading.");
        telemetry.update();
        return turnToHeading(0.0);
    }

    private boolean turnToHeading(double targetDegrees) throws InterruptedException {
        ElapsedTime timer = new ElapsedTime();
        int stableReadings = 0;
        while (canContinue() && timer.seconds() < AIM_TIMEOUT_SECONDS) {
            double error = normalizeDegrees(targetDegrees - yawDegrees());
            if (Math.abs(error) <= AIM_TOLERANCE_DEGREES) {
                stopDrive();
                if (++stableReadings >= 3) {
                    setDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    return true;
                }
            } else {
                stableReadings = 0;
                turnWithError(error);
            }
            sleep(20);
        }
        stopDrive();
        setDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);
        outcome = "IMU aim stopped: time limit";
        return false;
    }

    private void turnWithError(double errorDegrees) {
        setDriveMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        double magnitude = clamp(Math.abs(errorDegrees) * TURN_KP,
                MIN_TURN_POWER, MAX_TURN_POWER);
        double turn = Math.copySign(magnitude, errorDegrees);
        motors[0].setPower(-turn);
        motors[1].setPower(-turn);
        motors[2].setPower(turn);
        motors[3].setPower(turn);
    }

    private void setRunToPositionPowers(double basePower, double headingCorrection) {
        double left = clamp(basePower - headingCorrection, 0.12, 1.0);
        double right = clamp(basePower + headingCorrection, 0.12, 1.0);
        motors[0].setPower(left);
        motors[1].setPower(left);
        motors[2].setPower(right);
        motors[3].setPower(right);
    }

    private void setDriveMode(DcMotor.RunMode mode) {
        for (DcMotor motor : motors) motor.setMode(mode);
    }

    private double yawDegrees() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    private static double normalizeDegrees(double angle) {
        while (angle > 180) angle -= 360;
        while (angle <= -180) angle += 360;
        return angle;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private boolean moveTimed(double frontLeft, double backLeft, double frontRight,
                              double backRight, double seconds, String step)
            throws InterruptedException {
        if (!canContinue()) return false;
        for (DcMotor motor : motors) motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motors[0].setPower(frontLeft);
        motors[1].setPower(backLeft);
        motors[2].setPower(frontRight);
        motors[3].setPower(backRight);
        ElapsedTime timer = new ElapsedTime();
        while (canContinue() && timer.seconds() < seconds) {
            reportStep(step, "timed fallback");
            sleep(10);
        }
        stopDrive();
        if (!canContinue()) {
            outcome = step + " stopped: AUTO limit or STOP";
            return false;
        }
        outcome = step + " complete (timed)";
        return true;
    }

    private void reportStep(String step, String mode) {
        telemetry.addData("AUTO step", step);
        telemetry.addData("Drive mode", mode);
        telemetry.addData("Seconds remaining", "%.1f", AUTO_CUTOFF_SECONDS - matchClock.seconds());
        telemetry.update();
    }

    void reportMechanism(String name, String status) {
        telemetry.addData(name, status);
        telemetry.update();
    }

    boolean waitWhileActive(long milliseconds) throws InterruptedException {
        ElapsedTime timer = new ElapsedTime();
        while (canContinue() && timer.milliseconds() < milliseconds) sleep(10);
        return canContinue();
    }

    private boolean canContinue() {
        return opModeIsActive() && matchClock.seconds() < AUTO_CUTOFF_SECONDS;
    }

    private void stopDrive() {
        for (DcMotor motor : motors) {
            if (motor != null) motor.setPower(0);
        }
    }

    private void stopAll() {
        for (DcMotor motor : motors) {
            if (motor != null) {
                try { motor.setPower(0); }
                catch (RuntimeException ignored) { /* Continue stopping remaining devices. */ }
            }
        }
        if (mechanisms != null) {
            try { mechanisms.stopAll(); }
            catch (RuntimeException ignored) { /* OpMode shutdown also disables outputs. */ }
        }
    }
}
