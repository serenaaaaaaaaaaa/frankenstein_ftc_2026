package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Travel {
    private final DcMotor travelmotor;

    public Travel(DcMotor travelmotor) {
        this.travelmotor = travelmotor;
    }

    public void update(Gamepad gamepad) {
        // Intake when Right Trigger is held
        if (gamepad.circleWasPressed()) {
            travelmotor.setPower(-0.9);
        }

        // Stop: When released, continuous rotation servos return to stop (0.5)
        else {
            travelmotor.setPower(0.0);
        }
    }
}
