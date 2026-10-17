package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

public class Outtake {
    private final CRServo windmill;
    private final DcMotor flywheel;

    public Outtake(CRServo windmill, DcMotor flywheel) {
        this.windmill = windmill;
        this.flywheel = flywheel;
    }

    public void update(Gamepad gamepad) {
        // Intake when Right Trigger is held
        if (gamepad.dpad_up) {
            windmill.setPower(0.9);
            flywheel.setPower(0.9); // Mirrored rotation (0.0 to 1.0 bounds)
        }
        // Stop: When released, continuous rotation servos return to stop (0.5)
        else {
           windmill.setPower(0.0);
           flywheel.setPower(0.0);
        }
    }
}
