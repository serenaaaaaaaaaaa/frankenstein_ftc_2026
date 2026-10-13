package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Autonomous mechanism adapter.
 *
 * Existing intake hardware is supported now. Missing scoring hardware is represented by
 * disabled placeholders so route code can be completed without energizing unknown devices.
 */
final class BioBuzzMechanisms {
    private static final boolean ENABLE_PRELOAD_SCORING = false;
    private static final String LAUNCHER_MOTOR_NAME = "pollenLauncher";
    private static final double LAUNCHER_POWER = 0.0; // Filler: set only after direction testing.
    private static final long LAUNCHER_SPINUP_MS = 0; // Filler: measure on the finished launcher.
    private static final long FEED_MS = 0; // Filler: measure time needed to feed four POLLEN.

    private final CRServo leftIntake;
    private final CRServo rightIntake;
    private final DcMotor centreIntake;
    private final DcMotor launcher;

    BioBuzzMechanisms(HardwareMap hardwareMap) {
        leftIntake = hardwareMap.tryGet(CRServo.class, "leftIntake");
        rightIntake = hardwareMap.tryGet(CRServo.class, "rightIntake");
        centreIntake = hardwareMap.tryGet(DcMotor.class, "centreIntake");
        launcher = ENABLE_PRELOAD_SCORING
                ? hardwareMap.tryGet(DcMotor.class, LAUNCHER_MOTOR_NAME) : null;
        stopAll();
    }

    boolean intakeAvailable() {
        return leftIntake != null && rightIntake != null && centreIntake != null;
    }

    boolean preloadScoringReady() {
        return ENABLE_PRELOAD_SCORING && launcher != null && intakeAvailable()
                && Math.abs(LAUNCHER_POWER) > 0 && LAUNCHER_SPINUP_MS > 0 && FEED_MS > 0;
    }

    /**
     * Filler hook for a future HIVE shot. It remains a no-op until every scoring constant
     * is deliberately enabled. Returning false lets the route continue to PARK.
     */
    boolean scoreFourPreloads(BioBuzzAutoBase auto) throws InterruptedException {
        if (!preloadScoringReady()) {
            auto.reportMechanism("HIVE scoring", "SKIPPED - launcher filler is disabled");
            return false;
        }

        launcher.setPower(LAUNCHER_POWER);
        if (!auto.waitWhileActive(LAUNCHER_SPINUP_MS)) {
            stopAll();
            return false;
        }
        setFeedPower();
        boolean completed = auto.waitWhileActive(FEED_MS);
        stopAll();
        auto.reportMechanism("HIVE scoring", completed ? "feed complete" : "stopped early");
        return completed;
    }

    private void setFeedPower() {
        // Same outtake direction already used by the team's TeleOp Intake class.
        leftIntake.setPower(-1.0);
        rightIntake.setPower(1.0);
        centreIntake.setPower(1.0);
    }

    void stopAll() {
        if (leftIntake != null) leftIntake.setPower(0);
        if (rightIntake != null) rightIntake.setPower(0);
        if (centreIntake != null) centreIntake.setPower(0);
        if (launcher != null) launcher.setPower(0);
    }
}
