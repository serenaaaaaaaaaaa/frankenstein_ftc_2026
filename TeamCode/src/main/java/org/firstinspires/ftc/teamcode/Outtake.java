package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

public class Outtake {
    private static final double SHOOT_POWER = -0.9;
    private static final double REVERSE_POWER = 0.9;
    private static final double ANGLE_STEP = 0.10;

    private final DcMotor flywheel;
    private final Servo outservo;
    private boolean lastUp;
    private boolean lastDown;

    public Outtake(DcMotor flywheel, Servo outservo) {
        this.flywheel = flywheel;
        this.outservo = outservo;
    }

    public void update(Gamepad gamepad) {
        boolean up = gamepad.dpad_up;
        boolean down = gamepad.dpad_down;
        if (up && !lastUp && !down) {
            outservo.setPosition(Math.min(1.0, outservo.getPosition() + ANGLE_STEP));
        } else if (down && !lastDown && !up) {
            outservo.setPosition(Math.max(0.0, outservo.getPosition() - ANGLE_STEP));
        }
        lastUp = up;
        lastDown = down;

        double power = 0.0;
        if (gamepad.dpad_left != gamepad.dpad_right) {
            power = gamepad.dpad_right ? SHOOT_POWER : REVERSE_POWER;
        }
        flywheel.setPower(power);
    }
}

