# BIOBUZZ autonomous strategy

The robot has four selectable BIOBUZZ OpModes. They use encoder-controlled mecanum drive, IMU heading correction, optional AprilTag aiming, the flywheel, and the `travelmotor` feeder. The intake stays stopped during AUTO.

| Driver Station OpMode | Start | Current route | Current goal |
| --- | --- | --- | --- |
| `BIOBUZZ Red - Loading` | Red wall beside the red LOADING ZONE | LEAVE, aim, shoot four, strafe right | SCORE + PARK |
| `BIOBUZZ Blue - Loading` | Blue wall beside the blue LOADING ZONE | LEAVE, aim, shoot four, strafe right | SCORE + PARK |
| `BIOBUZZ Red - Garden` | Red garden-side wall | LEAVE, aim, shoot four, hold | SCORE + LEAVE |
| `BIOBUZZ Blue - Garden` | Blue garden-side wall | LEAVE, aim, shoot four, hold | SCORE + LEAVE |

For every route, the robot starts with its back touching the perimeter and its front facing the field center. For a loading route, stage immediately beside your alliance's LOADING ZONE, fully outside it, with the zone on the robot's right. Keep the route clear of the HIVE, alliance partner, and loose game elements.

## Movement and aiming

The robot now uses drive encoders with IMU heading correction. The baseline assumes 537.7 encoder ticks per motor revolution and 96 mm mecanum wheels. `FORWARD_CORRECTION` starts at `1.00`, while `STRAFE_CORRECTION` starts at `1.12` to account for mecanum slip.

Validate each correction with a 24-inch floor test. Update it using `old correction * commanded distance / measured distance`. For example, if a commanded 24 inches travels 22 inches, multiply that correction by `24 / 22`.

The IMU is configured for a REV Hub mounted logo-up with its USB port facing forward. Change the two `RevHubOrientationOnRobot` directions if the Control Hub is mounted differently. LEAVE and PARK correct yaw drift and include wheel-stall detection.

Before shooting, `Webcam 1` centers red HIVE tags 30-37 or blue HIVE tags 38-45 to within 1.5 degrees. If no alliance target is visible, the robot safely returns to its zero-degree starting heading. Tune `CAMERA_TO_LAUNCHER_OFFSET_DEGREES` if the webcam and launcher do not point in exactly the same direction.

## Scoring filler

`BioBuzzMechanisms.java` contains the current preload HIVE scoring sequence:

1. verify the flywheel and `travelmotor` feeder are configured;
2. spin up the flywheel at the tested TeleOp direction;
3. pulse `travelmotor` once for each of four POLLEN, with flywheel recovery time between shots;
4. stop every mechanism;
5. continue to PARK even when scoring is unavailable.

It is enabled with `ENABLE_HIVE_SCORING = true`. The code uses the tested `-0.9` direction for both motors, spins up for 1.2 seconds, feeds each POLLEN for 0.35 seconds, and gives the flywheel 0.45 seconds to recover between shots. These are filler timings that must be tuned on the robot. The AUTO start and one-second forward movement must leave the launcher aimed safely toward the HIVE. AprilTag aiming remains disabled.

Test the shot with the drivetrain raised and no POLLEN first, then test one POLLEN from a protected position. The code does not confirm a HIVE TIP or detect field obstacles.

## Game basis

The strategy uses the official BIOBUZZ Competition Manual TU04, current on 13 October 2026. AUTO lasts 30 seconds. LEAVE and AUTO PARK are assessed at the end of AUTO; PARK requires the robot to be at least partly in its alliance LOADING ZONE. The strategy prioritizes these repeatable objectives while HIVE scoring hardware is incomplete. Check the official manual and Team Updates again before each event because FIRST publishes revisions during the season.
