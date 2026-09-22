package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.hardware.Servo;

public class Intake {
    private final Servo leftIntake;
    private final Servo rightIntake;

    public Intake(Servo leftIntake, Servo rightIntake) {
        this.leftIntake = leftIntake;
        this.rightIntake = rightIntake;
    }
    public void update(Gamepad gamepad) {

        }
    }
}