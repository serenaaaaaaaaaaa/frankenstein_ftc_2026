package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.CRServo;

public class Intake {
    private final CRServo leftIntake;
    private final CRServo rightIntake;

    public Intake(CRServo leftIntake, CRServo rightIntake) {
        this.leftIntake = leftIntake;
        this.rightIntake = rightIntake;
    }

    public void update(Gamepad gamepad) {
        // Intake when Right Trigger is held
        if (gamepad.right_trigger > 0.1) {
            leftIntake.setPower(1.0);
            rightIntake.setPower(-1.0); // Mirrored rotation (0.0 to 1.0 bounds)
        }
        // Outtake when Left Trigger is held
        else if (gamepad.left_trigger > 0.1) {
            leftIntake.setPower(-1.0);
            rightIntake.setPower(1.0);
        }
        // Stop: When released, continuous rotation servos return to stop (0.5)
        else {
            leftIntake.setPower(0.0);
            rightIntake.setPower(0.0);
        }
    }
}
