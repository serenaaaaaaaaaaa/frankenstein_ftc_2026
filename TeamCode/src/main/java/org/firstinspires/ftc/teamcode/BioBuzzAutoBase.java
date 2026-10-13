package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

/** Shared strategy and motion controller for the four BIOBUZZ starting positions. */
public abstract class BioBuzzAutoBase extends LinearOpMode {
    // Encoder mode automatically replaces timed mode after BOTH measured scales are entered.
    private static final double FORWARD_TICKS_PER_INCH = 0.0;
    private static final double STRAFE_TICKS_PER_INCH = 0.0;

    private static final double DRIVE_POWER = 0.30;
    private static final double LEAVE_DISTANCE_INCHES = 18.0;
    private static final double LOADING_ZONE_STRAFE_INCHES = 8.0;

    // Working fallback values for the current uncalibrated robot. Tune on the real field.
    private static final double TIMED_LEAVE_SECONDS = 1.00;
    private static final double TIMED_PARK_STRAFE_SECONDS = 0.55;

    private static final double AUTO_CUTOFF_SECONDS = 29.0;
    private static final double STEP_TIMEOUT_SECONDS = 6.0;
    private static final double STALL_TIMEOUT_SECONDS = 1.25;
    private static final int TARGET_TOLERANCE_TICKS = 20;
    private static final int PROGRESS_TICKS = 5;

    private final boolean finishInLoadingZone;
    private final String startingPosition;
    private final DcMotor[] motors = new DcMotor[4];
    private final ElapsedTime matchClock = new ElapsedTime();
    private BioBuzzMechanisms mechanisms;
    private String outcome = "READY";

    protected BioBuzzAutoBase(boolean finishInLoadingZone, String startingPosition) {
        this.finishInLoadingZone = finishInLoadingZone;
        this.startingPosition = startingPosition;
    }

    @Override
    public final void runOpMode() throws InterruptedException {
        try {
            initializeHardware();
            while (opModeInInit()) {
                telemetry.addData("BIOBUZZ AUTO", startingPosition);
                telemetry.addData("Strategy", finishInLoadingZone
                        ? "LEAVE -> score filler -> PARK" : "LEAVE -> score filler -> HOLD");
                telemetry.addData("Drive mode", encoderMode() ? "ENCODER" : "TIMED FALLBACK");
                telemetry.addData("Intake", mechanisms.intakeAvailable() ? "FOUND / stopped" : "NOT FOUND");
                telemetry.addData("Launcher", mechanisms.preloadScoringReady()
                        ? "READY" : "FILLER DISABLED / scoring skipped");
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

            // Current hardware has an intake but no defined launcher. This hook reports SKIPPED
            // and immediately continues, so reliable LEAVE/PARK points remain the priority.
            mechanisms.scoreFourPreloads(this);
            if (!opModeIsActive()) return;

            if (finishInLoadingZone) {
                if (!moveRightForPark()) return;
                outcome = "LEAVE + PARK route complete";
            } else {
                outcome = "LEAVE route complete; holding position";
            }
        } finally {
            stopAll();
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
            motors[i].setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        mechanisms = new BioBuzzMechanisms(hardwareMap);
    }

    private boolean encoderMode() {
        return FORWARD_TICKS_PER_INCH > 0 && STRAFE_TICKS_PER_INCH > 0;
    }

    private boolean moveForwardForLeave() throws InterruptedException {
        return encoderMode()
                ? moveByEncoder(LEAVE_DISTANCE_INCHES, 0, "LEAVE")
                : moveTimed(DRIVE_POWER, DRIVE_POWER, DRIVE_POWER, DRIVE_POWER,
                        TIMED_LEAVE_SECONDS, "LEAVE");
    }

    private boolean moveRightForPark() throws InterruptedException {
        return encoderMode()
                ? moveByEncoder(0, LOADING_ZONE_STRAFE_INCHES, "PARK")
                : moveTimed(DRIVE_POWER, -DRIVE_POWER, -DRIVE_POWER, DRIVE_POWER,
                        TIMED_PARK_STRAFE_SECONDS, "PARK");
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
            reportStep(step, "encoder");
            sleep(10);
        }
        outcome = step + " stopped: time limit or STOP";
        stopDrive();
        return false;
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
