package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Intake {
    private final DcMotor centreIntake;

    public Intake(DcMotor centreIntake){
        this.centreIntake = centreIntake;
    }

    public void update(Gamepad gamepad) {
        // Intake when the left trigger is held.
        if (gamepad.left_trigger > 0.1) {
            centreIntake.setPower(-0.9);
        }
        // Reverse the intake when the right trigger is held.
        else if (gamepad.right_trigger > 0.1) {
            centreIntake.setPower(0.9);
        }
        // Stop: When released, continuous rotation servos return to stop (0.5)
        else {
            centreIntake.setPower(0.0);
        }
    }
}
