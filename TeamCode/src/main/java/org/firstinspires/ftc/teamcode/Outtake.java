package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.CRServo;

public class Outtake {
    private static final double SHOOT_POWER = -0.9;
    private static final double REVERSE_POWER = 0.9;
    private static final double ANGLE_STEP = 0.10;

    private final DcMotor flywheel;
    private final CRServo outservo;
    private boolean lastUp;
    private boolean lastDown;

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
        // Stop: When released, continuous rotation servos return to stop (0.5)
        else {
            flywheel.setPower(0.0);
            flywheel.setPower(0.0);
        }
    }
}

