package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

public class Outtake {
    private DcMotor flywheel;
    private Servo outservo;

    public Outtake(DcMotor flywheel, Servo outservo) {
        this.flywheel = flywheel;
        this.outservo = outservo; }

        public void update(Gamepad gamepad1) {
        if (gamepad1.dpad_up) {
            outservo.setPosition(1.0);
            flywheel.setPower(-0.9);
        }
        else if (gamepad1.dpad_down) {
            outservo.setPosition(0.0);
            flywheel.setPower(-0.9);
        }
        else {
            flywheel.setPower(0.0);
        }

    }
}

