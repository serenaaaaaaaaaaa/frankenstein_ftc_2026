package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

public class Outtake {
    private final DcMotor flywheel;

    public Outtake(DcMotor flywheel) {
        this.flywheel = flywheel;
    }

    public void update(Gamepad gamepad) {
        // Intake when Right Trigger is held
        if (gamepad.dpad_up) {
            flywheel.setPower(-0.9);
        }
        // Stop: When released, continuous rotation servos return to stop (0.5)
        else {
           flywheel.setPower(0.0);
        }
    }
}
