package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.CRServo;

public class Outtake {
    private final DcMotor flywheel;
    private final CRServo outservo;

    public Outtake(DcMotor flywheel, CRServo outservo) {
        this.flywheel = flywheel;
        this.outservo = outservo;
    }

    public void update(Gamepad gamepad1) {
        if (gamepad1.dpad_up) {
            outservo.setPower(1.0);
            flywheel.setPower(-0.9);
        }
        else if (gamepad1.dpad_down) {
            outservo.setPower(0.0);
            flywheel.setPower(-0.9);
        }
        else {
            flywheel.setPower(0.0);
            outservo.setPower(0.0);
        }
    }
}

