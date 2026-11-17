package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/** Autonomous adapter for the mechanisms currently configured in TeleOp. */
final class BioBuzzMechanisms {
    // The flywheel direction matches the tested Outtake.java control.
    private static final double FLYWHEEL_POWER = -0.9;

    // Enabled for robot testing. The AUTO start must place the outtake toward the HIVE.
    private static final boolean ENABLE_HIVE_SCORING = true;
    private static final long FLYWHEEL_SPINUP_MS = 1200; // Initial filler value; tune on robot.
    private static final int PRELOAD_COUNT = 4;
    private static final double TRAVEL_POWER = -0.9;
    private static final long FEED_PULSE_MS = 350; // Filler value; tune for one POLLEN.
    private static final long FLYWHEEL_RECOVERY_MS = 450; // Filler value; tune on robot.

    private final CRServo leftIntake;
    private final CRServo rightIntake;
    private final DcMotor centreIntake;
    private final DcMotor travelMotor;
    private final DcMotor flywheel;

    BioBuzzMechanisms(HardwareMap hardwareMap) {
        leftIntake = hardwareMap.tryGet(CRServo.class, "leftIntake");
        rightIntake = hardwareMap.tryGet(CRServo.class, "rightIntake");
        centreIntake = hardwareMap.tryGet(DcMotor.class, "centreIntake");
        travelMotor = hardwareMap.tryGet(DcMotor.class, "travelmotor");
        flywheel = hardwareMap.tryGet(DcMotor.class, "flywheel");
        stopAll();
    }

    boolean intakeAvailable() {
        return leftIntake != null && rightIntake != null && centreIntake != null;
    }

    boolean outtakeAvailable() {
        return flywheel != null;
    }

    boolean preloadScoringReady() {
        return ENABLE_HIVE_SCORING && outtakeAvailable() && travelMotor != null
                && FLYWHEEL_SPINUP_MS > 0 && PRELOAD_COUNT > 0
                && FEED_PULSE_MS > 0 && FLYWHEEL_RECOVERY_MS > 0;
    }

    /** Spins up the flywheel, then pulses the travel feeder once per preload. */
    boolean scoreFourPreloads(BioBuzzAutoBase auto) throws InterruptedException {
        if (!preloadScoringReady()) {
            auto.reportMechanism("HIVE scoring", "SKIPPED - flywheel or travel feeder missing");
            return false;
        }

        flywheel.setPower(FLYWHEEL_POWER);
        if (!auto.waitWhileActive(FLYWHEEL_SPINUP_MS)) {
            stopAll();
            return false;
        }

        for (int shot = 1; shot <= PRELOAD_COUNT; shot++) {
            auto.reportMechanism("HIVE scoring", "feeding POLLEN " + shot + "/" + PRELOAD_COUNT);
            travelMotor.setPower(TRAVEL_POWER);
            if (!auto.waitWhileActive(FEED_PULSE_MS)) {
                stopAll();
                return false;
            }
            travelMotor.setPower(0);

            if (shot < PRELOAD_COUNT && !auto.waitWhileActive(FLYWHEEL_RECOVERY_MS)) {
                stopAll();
                return false;
            }
        }

        stopAll();
        auto.reportMechanism("HIVE scoring", "four preload shots complete");
        return true;
    }

    void stopAll() {
        if (leftIntake != null) leftIntake.setPower(0);
        if (rightIntake != null) rightIntake.setPower(0);
        if (centreIntake != null) centreIntake.setPower(0);
        if (travelMotor != null) travelMotor.setPower(0);
        if (flywheel != null) flywheel.setPower(0);
    }
}
