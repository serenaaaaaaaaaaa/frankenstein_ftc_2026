package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.List;

/**
 * BaseClass is now the main TeleOp OpMode that runs the Drive and Linear Slide modules.
 */
@TeleOp(name = "Robot: Main Control", group = "TeleOp")
public class BaseClass extends LinearOpMode {

    // Drivetrain Motors
    protected DcMotor backLeftMotor = null;
    protected DcMotor backRightMotor = null;
    protected DcMotor frontLeftMotor = null;
    protected DcMotor frontRightMotor = null;

    // Linear Slide Motors
    //protected DcMotor leftSlide = null;
    //protected DcMotor rightSlide = null;

    // Intake Motors
    protected DcMotor centreIntake = null;

    //Travel Motors
    protected DcMotor travelmotor = null;

    // Outtake Motors
    protected DcMotor flywheel;
    protected Servo outservo;

    // Component Instances
    private Drive driveModule;
    //private Linear linearModule;
    private Intake intakeModule;
    private Travel travelModule;
    private Outtake outtakeModule;
    //private AprilTagReader aprilTagReader;

    // Global Variables
    public ElapsedTime matchTime = new ElapsedTime();

    @Override
    public void runOpMode() throws InterruptedException {
        // 1. Initialize all hardware
        initHardware();

        // 2. Initialize the system modules
        driveModule = new Drive(frontLeftMotor, backLeftMotor, frontRightMotor, backRightMotor);
        //linearModule = new Linear(leftSlide, rightSlide);
        intakeModule = new Intake(centreIntake);
        travelModule = new Travel(travelmotor);
        if (flywheel != null && outservo != null) {
            outtakeModule = new Outtake(flywheel, outservo);
        }

        telemetry.addData("Status", "Initialized. Ready to start.");
        telemetry.addData("Outtake", outtakeModule == null ? "Check flywheel and outservo" : "Ready");
        telemetry.update();

        waitForStart();
        matchTime.reset();

        // 3. Main execution loop
        while (opModeIsActive()) {
            // Update components with gamepad inputs
            driveModule.update(gamepad1); // Left stick drives, bumpers scale speed
            //linearModule.update(gamepad1);
            intakeModule.update(gamepad1);
            travelModule.update(gamepad1);
            if (outtakeModule != null) outtakeModule.update(gamepad1);

            // Display status and telemetry profiles
            telemetry.addData("Status", "Running");
            telemetry.addData("Drive speed", "%.0f%%", driveModule.speedMultiplier * 100);
            if (outtakeModule != null) {
                double servoPosition = outservo.getPosition();
                telemetry.addData("Outtake servo target", "%.2f (%.0f%%)", servoPosition, servoPosition * 100);
                double power = flywheel.getPower();
                telemetry.addData("Flywheel command", power < 0 ? "Forward" : power > 0 ? "Reverse" : "Off");
            } else {
                telemetry.addData("Outtake", "Check flywheel and outservo configuration");
            }
            //telemetry.addData("Slide Target Position", linearModule.getTargetPosition());
           // telemetry.addData("Slide Real Pos", "L: %d | R: %d",
            //        linearModule.getLeftCurrentPosition(), linearModule.getRightCurrentPosition());
            //telemetry.addData("Slide Power", "L: %d | R: %d",
            //        linearModule.getLeftPower(), linearModule.getRightPower());
            telemetry.update();
        }

        // 4. Cleanup: Auto-retract linear slides when the user stops the OpMode
        telemetry.addData("Status", "Retracting Slides Safely...");
        telemetry.update();
    }

    /**
     * Maps hardware from the configuration and sets initial states.
     */
    public void initHardware() {
        // Hubs: Enable bulk caching for better performance
        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        // Drivetrain: Map motors (make sure these names match your config!)
        backLeftMotor = hardwareMap.get(DcMotor.class, "backLeftMotor");
        backRightMotor = hardwareMap.get(DcMotor.class, "backRightMotor");
        frontLeftMotor = hardwareMap.get(DcMotor.class, "frontLeftMotor");
        frontRightMotor = hardwareMap.get(DcMotor.class, "frontRightMotor");

        // Set drivetrain to run without encoders
        backLeftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRightMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontLeftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRightMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // Slide Hardware Mapping
        //leftSlide = hardwareMap.get(DcMotor.class, "leftSlide");
        //rightSlide = hardwareMap.get(DcMotor.class, "rightSlide");

        centreIntake = hardwareMap.get(DcMotor.class, "centreIntake");

        travelmotor = hardwareMap.get(DcMotor.class, "travelmotor");

        flywheel = hardwareMap.tryGet(DcMotor.class, "flywheel");
        outservo = hardwareMap.tryGet(Servo.class, "outservo");
        if (flywheel != null) flywheel.setPower(0);

        //naprilTagReader = new AprilTagReader(hardwareMap);
        
        // Encoders initialization is fully handled internally by the Linear class constructor
    }
}
