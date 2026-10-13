# BIOBUZZ autonomous routes

Choose the OpMode matching **your alliance and starting corner** on the Driver Station. Enable its **30-second AUTO timer**. The four routines use only the four drive motors, with the existing configuration names `frontLeftMotor`, `backLeftMotor`, `frontRightMotor`, and `backRightMotor`. They do not move the lift or intake.

| Driver Station OpMode | Start with back touching | Route | Intended AUTO result |
| --- | --- | --- | --- |
| BIOBUZZ Red - Loading | Top wall, immediately beside the red top-left LOADING ZONE | Forward, then strafe right into red zone | LEAVE + PARK |
| BIOBUZZ Blue - Loading | Bottom wall, immediately beside the blue bottom-right LOADING ZONE | Forward, then strafe right into blue zone | LEAVE + PARK |
| BIOBUZZ Red - Garden | Bottom wall, toward the red bottom-left GARDEN | Forward | LEAVE |
| BIOBUZZ Blue - Garden | Top wall, toward the blue top-right GARDEN | Forward | LEAVE |

In every case, face the front of the robot toward the **center of the field**. Keep the entire robot outside the LOADING ZONE at the start. For the two loading routes, stage immediately beside the zone so it is on the robot's **right**; the path assumes the robot has room to move forward and right without hitting a wall, HIVE, partner, or another scoring element. Do not use these routes from a different starting tile. The garden routes deliberately stop after LEAVE because the alliance LOADING ZONE is at the far end of the field; reaching it without position sensing and a surveyed route would be unreliable.

## Before any motion

Edit `BioBuzzAutoBase.java`:

1. Measure and set `FORWARD_TICKS_PER_INCH`. Drive forward a measured distance on a safe test surface, and divide the average absolute encoder change of the four wheels by inches traveled.
2. For a loading route, measure and set `STRAFE_TICKS_PER_INCH` separately. Mecanum sideways travel generally needs a different scale.
3. Start with the robot on blocks and verify all four wheels move in the intended direction. The code assumes the **right motors are reversed**. Confirm encoder counts advance toward their commanded targets.
4. Put the robot in its exact intended field starting position and tune `LEAVE_DISTANCE_INCHES` and `LOADING_ZONE_STRAFE_INCHES` at low power. The included 18-inch forward and 8-inch right commands are **initial estimates**, not field-validated distances. The robot's width, length, starting offset, wheel size, and wheel slip determine whether PARK is actually achieved. The 8-inch sideways estimate leaves a nominal 3-inch margin from the side wall when the robot starts directly beside the 11-inch-wide zone; verify actual clearance before increasing it.
5. Test each route on the real field, then verify it finishes away from the wall (LEAVE) and at least partly inside the matching LOADING ZONE (PARK where applicable). Leave enough clearance for the alliance partner and HIVE.

With an unset encoder scale, the selected routine displays `NOT SET` during INIT and **does not drive** after START. It uses no gamepad input during AUTO. A wheel that fails to make progress, a step taking over six seconds, STOP, or the 29-second cutoff ends motion. Power is set to zero even if the OpMode exits early.

## Game basis and limits

The supplied *BIOBUZZ Competition Manual TU01* defines a 30-second AUTO period (10.4), awards LEAVE when the robot no longer touches the perimeter wall and PARK when it is partly in its own LOADING ZONE (10.5.4), and scores 3 and 5 points respectively (Table 10-2). Figure 9-4 shows the red loading zone in the top-left and blue loading zone in the bottom-right. The garden-side routes stay on their own alliance half, consistent with G402. G304 requires the robot to start touching a perimeter wall, outside the LOADING ZONE, and contacting exactly four POLLEN preloads. G403 requires the robot to be motionless during the AUTO-to-TELEOP transition.

This version does not attempt HIVE TIPS or POLLEN scoring. The synced `Intake.java` has no actions, and no launcher geometry, servo positions, aiming sensor, or tested intake mechanism was supplied. The routes require physical testing before competition use; encoder targets alone do not detect another robot or field obstacle.
