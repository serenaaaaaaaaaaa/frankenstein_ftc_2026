# BIOBUZZ autonomous strategy

The robot now has four selectable BIOBUZZ OpModes. They use the drivetrain that exists today and recognize the existing three-part intake (`leftIntake`, `rightIntake`, and `centreIntake`). The intake stays stopped because there is no confirmed launcher or aiming setup yet.

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

`BioBuzzMechanisms.java` contains the complete placeholder sequence for preload HIVE scoring:

1. verify the intake and future launcher are configured;
2. spin up the launcher;
3. run the existing intake in its TeleOp outtake direction to feed four preload POLLEN;
4. stop every mechanism;
5. continue to PARK even when scoring is unavailable.

It is disabled with `ENABLE_PRELOAD_SCORING = false`. The launcher name, direction/power, spin-up time, feed time, physical launch position, and aiming method are unknown. While disabled, telemetry reports `HIVE scoring: SKIPPED` and the intake never moves. This gives us a safe place to add the real mechanism later without changing the route structure.

Do not enable the filler until the launcher exists and every placeholder constant is tested on the robot. The current code does not claim a HIVE TIP or detect field obstacles.

## Game basis

The strategy uses the official BIOBUZZ Competition Manual TU04, current on 13 October 2026. AUTO lasts 30 seconds. LEAVE and AUTO PARK are assessed at the end of AUTO; PARK requires the robot to be at least partly in its alliance LOADING ZONE. The strategy prioritizes these repeatable objectives while HIVE scoring hardware is incomplete. Check the official manual and Team Updates again before each event because FIRST publishes revisions during the season.
