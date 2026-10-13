package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

/** Shared wheel-encoder motion for the four BIOBUZZ starting positions. */
public abstract class BioBuzzAutoBase extends LinearOpMode {
    // Measure these on YOUR robot before enabling an autonomous route.
    // Divide wheel encoder change by inches traveled; mecanum strafe needs its own scale.
    private static final double FORWARD_TICKS_PER_INCH = 0.0;
    private static final double STRAFE_TICKS_PER_INCH = 0.0;
    private static final double DRIVE_POWER = 0.30;
    private static final double LEAVE_DISTANCE_INCHES = 18.0;
    private static final double LOADING_ZONE_STRAFE_INCHES = 8.0;
    private static final double AUTO_CUTOFF_SECONDS = 29.0;
    private static final double STEP_TIMEOUT_SECONDS = 6.0;
    private static final double STALL_TIMEOUT_SECONDS = 1.25;
    private static final int TARGET_TOLERANCE_TICKS = 20;
    private static final int PROGRESS_TICKS = 5;

    private final boolean finishInLoadingZone;
    private final String startingPosition;
    private final DcMotor[] motors = new DcMotor[4];
    private final ElapsedTime matchClock = new ElapsedTime();
    private String outcome = "READY";

    protected BioBuzzAutoBase(boolean finishInLoadingZone, String startingPosition) {
        this.finishInLoadingZone = finishInLoadingZone;
        this.startingPosition = startingPosition;
    }

    @Override
    public final void runOpMode() throws InterruptedException {
        try {
            String[] names = {"frontLeftMotor", "backLeftMotor", "frontRightMotor", "backRightMotor"};
            for (int i = 0; i < motors.length; i++) {
                motors[i] = hardwareMap.get(DcMotor.class, names[i]);
                motors[i].setPower(0);
                motors[i].setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                motors[i].setDirection(i < 2
                        ? DcMotorSimple.Direction.FORWARD : DcMotorSimple.Direction.REVERSE);
                motors[i].setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }

            while (opModeInInit()) {
                telemetry.addData("BIOBUZZ AUTO", startingPosition);
                telemetry.addData("Route", finishInLoadingZone ? "LEAVE, then AUTO PARK" : "LEAVE");
                telemetry.addLine("Back of robot touches the starting wall; front faces field center.");
                if (finishInLoadingZone) {
                    telemetry.addLine("Start immediately beside YOUR loading zone, fully outside it.");
                    telemetry.addLine("The loading zone must be to the robot's RIGHT.");
                } else {
                    telemetry.addLine("Start on your garden-side end wall, clear of the HIVE.");
                }
                boolean ready = FORWARD_TICKS_PER_INCH > 0
                        && (!finishInLoadingZone || STRAFE_TICKS_PER_INCH > 0);
                telemetry.addData("Encoder scales", ready
                        ? "configured" : "NOT SET: robot will remain still");
                telemetry.update();
                sleep(20);
            }
            waitForStart();
            if (isStopRequested()) return;
            if (FORWARD_TICKS_PER_INCH <= 0
                    || (finishInLoadingZone && STRAFE_TICKS_PER_INCH <= 0)) {
                outcome = "No motion: set measured encoder scales";
                telemetry.addData("AUTO", outcome);
                telemetry.update();
                return;
            }
            matchClock.reset();
            if (!move(LEAVE_DISTANCE_INCHES, 0, "LEAVE")) return;
            if (finishInLoadingZone) {
                move(0, LOADING_ZONE_STRAFE_INCHES, "AUTO PARK");
            }
        } finally {
            stopAll();
            telemetry.addData("BIOBUZZ AUTO", outcome);
            telemetry.update();
        }
    }

    /** Positive forward or right; each wheel receives its own relative target. */
    private boolean move(double forwardInches, double rightInches, String step)
            throws InterruptedException {
        if (!opModeIsActive() || matchClock.seconds() >= AUTO_CUTOFF_SECONDS) return false;
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
        while (opModeIsActive() && matchClock.seconds() < AUTO_CUTOFF_SECONDS
                && stepClock.seconds() < STEP_TIMEOUT_SECONDS) {
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
                    stopAll();
                    return false;
                }
            }
            if (allFinished) {
                stopAll();
                outcome = step + " complete";
                return true;
            }
            telemetry.addData("AUTO step", step);
            telemetry.addData("Seconds remaining", "%.1f", AUTO_CUTOFF_SECONDS - matchClock.seconds());
            telemetry.update();
            sleep(10);
        }
        outcome = step + " stopped: time limit or STOP";
        stopAll();
        return false;
    }

    private void stopAll() {
        for (DcMotor motor : motors) {
            if (motor != null) {
                try { motor.setPower(0); }
                catch (RuntimeException ignored) { /* Another hub may still need its stop command. */ }
            }
        }
    }
}
