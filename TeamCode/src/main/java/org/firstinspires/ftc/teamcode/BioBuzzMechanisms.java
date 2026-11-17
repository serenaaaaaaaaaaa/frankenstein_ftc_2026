package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/** Autonomous adapter for the mechanisms currently configured in TeleOp. */
final class BioBuzzMechanisms {
    // The windmill and flywheel directions match the tested Outtake.java controls.
    private static final double FLYWHEEL_POWER = -0.9;
    private static final double WINDMILL_POWER = -0.9;

    // Enabled for robot testing. The AUTO start must place the outtake toward the HIVE.
    private static final boolean ENABLE_HIVE_SCORING = true;
    private static final long FLYWHEEL_SPINUP_MS = 1200; // Initial filler value; tune on robot.
    private static final long FOUR_POLLEN_FEED_MS = 2500; // Initial filler value; tune on robot.

    // Travel has no encoder/limit logic yet. It must not move autonomously until timed safely.
    private static final boolean ENABLE_TRAVEL_POSITIONING = false;
    private static final double TRAVEL_POWER = -0.9;
    private static final long TRAVEL_TO_SHOOT_MS = 0;

    private final CRServo leftIntake;
    private final CRServo rightIntake;
    private final DcMotor centreIntake;
    private final DcMotor travelMotor;
    private final CRServo windmill;
    private final DcMotor flywheel;

    BioBuzzMechanisms(HardwareMap hardwareMap) {
        leftIntake = hardwareMap.tryGet(CRServo.class, "leftIntake");
        rightIntake = hardwareMap.tryGet(CRServo.class, "rightIntake");
        centreIntake = hardwareMap.tryGet(DcMotor.class, "centreIntake");
        travelMotor = hardwareMap.tryGet(DcMotor.class, "travelmotor");
        windmill = hardwareMap.tryGet(CRServo.class, "windmill");
        flywheel = hardwareMap.tryGet(DcMotor.class, "flywheel");
        stopAll();
    }

    boolean intakeAvailable() {
        return leftIntake != null && rightIntake != null && centreIntake != null;
    }

    boolean outtakeAvailable() {
        return windmill != null && flywheel != null;
    }

    boolean preloadScoringReady() {
        boolean travelReady = !ENABLE_TRAVEL_POSITIONING
                || (travelMotor != null && TRAVEL_TO_SHOOT_MS > 0);
        return ENABLE_HIVE_SCORING && outtakeAvailable() && travelReady
                && FLYWHEEL_SPINUP_MS > 0 && FOUR_POLLEN_FEED_MS > 0;
    }

    /** Spins the tested flywheel, then feeds four preloads with the windmill. */
    boolean scoreFourPreloads(BioBuzzAutoBase auto) throws InterruptedException {
        if (!preloadScoringReady()) {
            auto.reportMechanism("HIVE scoring", outtakeAvailable()
                    ? "SKIPPED - route/timing not field-tested"
                    : "SKIPPED - outtake hardware missing");
            return false;
        }

        if (ENABLE_TRAVEL_POSITIONING) {
            travelMotor.setPower(TRAVEL_POWER);
            if (!auto.waitWhileActive(TRAVEL_TO_SHOOT_MS)) {
                stopAll();
                return false;
            }
            travelMotor.setPower(0);
        }

        flywheel.setPower(FLYWHEEL_POWER);
        if (!auto.waitWhileActive(FLYWHEEL_SPINUP_MS)) {
            stopAll();
            return false;
        }

        windmill.setPower(WINDMILL_POWER);
        boolean completed = auto.waitWhileActive(FOUR_POLLEN_FEED_MS);
        stopAll();
        auto.reportMechanism("HIVE scoring", completed ? "four-POLLEN feed complete" : "stopped early");
        return completed;
    }

    void stopAll() {
        if (leftIntake != null) leftIntake.setPower(0);
        if (rightIntake != null) rightIntake.setPower(0);
        if (centreIntake != null) centreIntake.setPower(0);
        if (travelMotor != null) travelMotor.setPower(0);
        if (windmill != null) windmill.setPower(0);
        if (flywheel != null) flywheel.setPower(0);
    }
}
