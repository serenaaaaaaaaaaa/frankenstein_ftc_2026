
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.Range;

public class Drive {

    private final DcMotor frontLeftMotor;
    private final DcMotor backLeftMotor;
    private final DcMotor frontRightMotor;
    private final DcMotor backRightMotor;

    public double speedMultiplier = 1.0;

    private boolean lastRB = false;
    private boolean lastLB = false;

    // Vision aiming settings
    private static final double AIM_KP = 0.018;
    private static final double AIM_TOLERANCE = 1.5;
    private static final double MAX_AIM_POWER = 0.35;

    // Change to -1 if testing shows the robot turns away from the tag
    private static final double AIM_DIRECTION = 1.0;

    public Drive(
            DcMotor frontLeft,
            DcMotor backLeft,
            DcMotor frontRight,
            DcMotor backRight
    ) {

        this.frontLeftMotor = frontLeft;
        this.backLeftMotor = backLeft;
        this.frontRightMotor = frontRight;
        this.backRightMotor = backRight;

        // Reverse right-side motors
        this.frontRightMotor.setDirection(DcMotor.Direction.REVERSE);
        this.backRightMotor.setDirection(DcMotor.Direction.REVERSE);
    }

    /**
     * Main drivetrain update.
     * Supports manual driving and automatic AprilTag aiming.
     */
    public void update(
            Gamepad gamepad
    ) {

        // Speed adjustment
        boolean currentRB = gamepad.right_bumper;
        boolean currentLB = gamepad.left_bumper;

        if (currentRB && !lastRB) {
            speedMultiplier =
                    Math.min(1.0, speedMultiplier + 0.1);
        }

        if (currentLB && !lastLB) {
            speedMultiplier =
                    Math.max(0.1, speedMultiplier - 0.1);
        }

        lastRB = currentRB;
        lastLB = currentLB;

        // Manual movement
        double drive = -gamepad.left_stick_y;
        double strafe = gamepad.left_stick_x;

        double turn;

        // Manual rotation takes priority
        if (Math.abs(gamepad.right_stick_x) > 0.08) {

            turn = gamepad.right_stick_x;

        }

        // Automatic AprilTag aiming
        else if (autoAim && targetVisible) {

            if (Math.abs(targetBearing) <= AIM_TOLERANCE) {

                turn = 0;

            } else {

                turn = targetBearing * AIM_KP * AIM_DIRECTION;

                turn = Range.clip(
                        turn,
                        -MAX_AIM_POWER,
                        MAX_AIM_POWER
                );
            }

        } else {

            // No target or aiming disabled
            turn = 0;
        }

        // Mecanum calculations
        double fLeftPow =
                drive + turn + strafe;

        double bLeftPow =
                drive + turn - strafe;

        double fRightPow =
                drive - turn - strafe;

        double bRightPow =
                drive - turn + strafe;

        // Apply speed multiplier
        fLeftPow *= speedMultiplier;
        bLeftPow *= speedMultiplier;
        fRightPow *= speedMultiplier;
        bRightPow *= speedMultiplier;

        // Normalize motor powers together
        double maxPower = Math.max(
                Math.max(Math.abs(fLeftPow), Math.abs(bLeftPow)),
                Math.max(Math.abs(fRightPow), Math.abs(bRightPow))
        );

        if (maxPower > 1.0) {

            fLeftPow /= maxPower;
            bLeftPow /= maxPower;
            fRightPow /= maxPower;
            bRightPow /= maxPower;
        }

        // Apply motor powers
        frontLeftMotor.setPower(fLeftPow);
        backLeftMotor.setPower(bLeftPow);
        frontRightMotor.setPower(fRightPow);
        backRightMotor.setPower(bRightPow);
    }

    // Stop drivetrain
    public void stop() {

        frontLeftMotor.setPower(0);
        backLeftMotor.setPower(0);
        frontRightMotor.setPower(0);
        backRightMotor.setPower(0);
    }
}
