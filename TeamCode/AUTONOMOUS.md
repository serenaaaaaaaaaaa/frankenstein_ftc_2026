# BIOBUZZ autonomous strategy

The robot has four selectable BIOBUZZ OpModes. They use the drivetrain and recognize the current intake, travel motor, flywheel, and optional webcam. The intake and travel motor stay stopped during the current route. HIVE scoring uses the flywheel only; there is no windmill feeder on this robot.

| Driver Station OpMode | Start | Current route | Current goal |
| --- | --- | --- | --- |
| `BIOBUZZ Red - Loading` | Red wall beside the red LOADING ZONE | Forward, scoring placeholder, strafe right | LEAVE + PARK |
| `BIOBUZZ Blue - Loading` | Blue wall beside the blue LOADING ZONE | Forward, scoring placeholder, strafe right | LEAVE + PARK |
| `BIOBUZZ Red - Garden` | Red garden-side wall | Forward, scoring placeholder, hold | LEAVE |
| `BIOBUZZ Blue - Garden` | Blue garden-side wall | Forward, scoring placeholder, hold | LEAVE |

For every route, the robot starts with its back touching the perimeter and its front facing the field center. For a loading route, stage immediately beside your alliance's LOADING ZONE, fully outside it, with the zone on the robot's right. Keep the route clear of the HIVE, alliance partner, and loose game elements.

## What works now

Until encoder calibration is available, the robot uses a conservative timed fallback:

- drive forward at 30% power for 1.00 second to attempt LEAVE;
- for loading starts, strafe right at 30% power for 0.55 second to attempt PARK;
- stop every drive and intake output before the OpMode exits;
- abort powered motion at STOP or before the 30-second AUTO period ends.

These times are filler measurements, so test with wheels raised first and then on a marked practice field. Tune `TIMED_LEAVE_SECONDS` and `TIMED_PARK_STRAFE_SECONDS` in `BioBuzzAutoBase.java`. Confirm the robot clears the perimeter wall for LEAVE and finishes partly inside its own LOADING ZONE for PARK.

## Encoder upgrade already wired in

Enter measured `FORWARD_TICKS_PER_INCH` and `STRAFE_TICKS_PER_INCH` in `BioBuzzAutoBase.java`. Once both values are above zero, all four routines automatically switch from timed motion to relative encoder targets with wheel-stall detection.

To measure a scale, record all four drive encoder positions, move a measured distance, calculate each absolute encoder change, average the four changes, and divide by inches moved. Measure forward and sideways separately.

## Scoring filler

`BioBuzzMechanisms.java` contains the current flywheel-only preload HIVE scoring sequence:

1. verify the flywheel and optional travel positioning are configured;
2. optionally move the travel mechanism to its shooting position;
3. spin up the flywheel at the tested TeleOp direction;
4. keep the flywheel running through a 2.5-second launch window;
5. stop every mechanism;
6. continue to PARK even when scoring is unavailable.

It is enabled with `ENABLE_HIVE_SCORING = true`. The code uses the existing `-0.9` flywheel direction, spins up for 1.2 seconds, and runs for another 2.5 seconds. The AUTO start and one-second forward movement must leave the launcher aimed safely toward the HIVE. Travel positioning and AprilTag aiming remain disabled. Confirm that preloaded POLLEN reaches the flywheel without a powered feeder before relying on this shot in competition.

Test the shot with the drivetrain raised and no POLLEN first, then test one POLLEN from a protected position. The code does not confirm a HIVE TIP or detect field obstacles.

## Game basis

The strategy uses the official BIOBUZZ Competition Manual TU04, current on 13 October 2026. AUTO lasts 30 seconds. LEAVE and AUTO PARK are assessed at the end of AUTO; PARK requires the robot to be at least partly in its alliance LOADING ZONE. The strategy prioritizes these repeatable objectives while HIVE scoring hardware is incomplete. Check the official manual and Team Updates again before each event because FIRST publishes revisions during the season.
