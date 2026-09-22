package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.Range;

public class Linear {
    private final DcMotor leftSlide;
    private final DcMotor rightSlide;

    // Safety limits for standard goBILDA Linear Actuator Kit (1120 Series)
    // - Motor: 5.2:1 Yellow Jacket (145.1 Ticks per Output Revolution)
    // - Lead Screw: 8mm travel per output revolution
    // - Resolution: ~18.14 ticks per mm
    // - Stroke Length: 203mm max travel -> 203mm * 18.14 ticks/mm = ~3682 ticks
    private static final int MIN_POSITION = 0;
    private static final int MAX_POSITION = 3500;
    private static final double SLIDE_POWER = 1.0; // Linear actuators require full power for heavy thrust loads

    private int targetPosition = 0;
    private boolean lastX = false;
    private boolean isUp = false;

    public Linear(DcMotor leftSlide, DcMotor rightSlide) {
        this.leftSlide = leftSlide;
        this.rightSlide = rightSlide;

        // Reset and prepare encoders
        this.leftSlide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.rightSlide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // Reverse one side if motors are mounted mirrored facing each other
        this.rightSlide.setDirection(DcMotor.Direction.REVERSE);
        this.leftSlide.setDirection(DcMotor.Direction.REVERSE);

        this.leftSlide.setTargetPosition(MIN_POSITION);
        this.rightSlide.setTargetPosition(MIN_POSITION);

        this.leftSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        this.rightSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    public void update(Gamepad gamepad) {
        // Toggle logic for X button (rising-edge detection)
        if (gamepad.x && !lastX) {
            isUp = !isUp;
            targetPosition = isUp ? MAX_POSITION : MIN_POSITION;
        }
        lastX = gamepad.x;

        // Apply strict safety bounds
        targetPosition = Range.clip(targetPosition, MIN_POSITION, MAX_POSITION);

        // Feed target positions to the motor controllers
        leftSlide.setTargetPosition(targetPosition);
        rightSlide.setTargetPosition(targetPosition);

        // Maintain constant holding power
        leftSlide.setPower(SLIDE_POWER);
        rightSlide.setPower(SLIDE_POWER);

    }

    public int getTargetPosition() {
        return targetPosition;
    }

    public int getLeftCurrentPosition() {
        return leftSlide.getCurrentPosition();
    }

    public int getRightCurrentPosition() {
        return rightSlide.getCurrentPosition();
    }

}
