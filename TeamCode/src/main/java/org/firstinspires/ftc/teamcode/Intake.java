package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Intake Module: Controls dual-servo intake wheels.
 * Handles running the intake via bumpers and triggers with stop-on-release behavior.
 */
public class Intake {
    private final Servo leftIntake;
    private final Servo rightIntake;

    public Intake(Servo leftIntake, Servo rightIntake) {
        this.leftIntake = leftIntake;
        this.rightIntake = rightIntake;
    }

    public void update(Gamepad gamepad) {
        // Intake when Right Trigger is held
        if (gamepad.right_trigger > 0.1) {
            leftIntake.setPosition(1.0);
            rightIntake.setPosition(0.0); // Mirrored rotation (0.0 to 1.0 bounds)
        } 
        // Outtake when Left Trigger is held
        else if (gamepad.left_trigger > 0.1) {
            leftIntake.setPosition(0.0);
            rightIntake.setPosition(1.0);
        } 
        // Stop: When released, continuous rotation servos return to stop (0.5)
        else {
            leftIntake.setPosition(0.5);
            rightIntake.setPosition(0.5);
        }
    }
}
