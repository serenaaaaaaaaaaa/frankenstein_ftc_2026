package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

/** Optional webcam/AprilTag helper. Missing camera hardware leaves vision disabled. */
public final class Camera implements AutoCloseable {
    public static final String DEFAULT_WEBCAM_NAME = "Webcam 1";

    private final AprilTagProcessor aprilTag;
    private final VisionPortal visionPortal;

    public Camera(HardwareMap hardwareMap) {
        WebcamName webcam = hardwareMap.tryGet(WebcamName.class, DEFAULT_WEBCAM_NAME);
        if (webcam == null) {
            aprilTag = null;
            visionPortal = null;
            return;
        }

        aprilTag = new AprilTagProcessor.Builder().build();
        visionPortal = new VisionPortal.Builder()
                .setCamera(webcam)
                .addProcessor(aprilTag)
                .build();
    }

    public boolean isAvailable() {
        return visionPortal != null && aprilTag != null;
    }

    /** Returns the requested tag, or the first known tag when targetId is negative. */
    public AprilTagDetection findTag(int targetId) {
        if (aprilTag == null) return null;
        List<AprilTagDetection> detections = aprilTag.getDetections();
        for (AprilTagDetection detection : detections) {
            if (targetId < 0 || detectionId(detection) == targetId) {
                return detection;
            }
        }
        return null;
    }

    /** SDK 12 moved id onto AprilTagSingleDetection; reflection keeps SDK 11 compatibility. */
    private static int detectionId(AprilTagDetection detection) {
        try {
            return detection.getClass().getField("id").getInt(detection);
        } catch (ReflectiveOperationException ignored) {
            return Integer.MIN_VALUE;
        }
    }

    public double bearingDegrees(int targetId) {
        AprilTagDetection detection = findTag(targetId);
        return detection != null && detection.ftcPose != null
                ? detection.ftcPose.bearing : Double.NaN;
    }

    /** Returns the bearing of the closest-to-center requested tag. */
    public double bestBearingDegrees(int[] targetIds) {
        if (aprilTag == null) return Double.NaN;
        double bestBearing = Double.NaN;
        for (AprilTagDetection detection : aprilTag.getDetections()) {
            if (detection.ftcPose == null || !contains(targetIds, detectionId(detection))) continue;
            double bearing = detection.ftcPose.bearing;
            if (Double.isNaN(bestBearing) || Math.abs(bearing) < Math.abs(bestBearing)) {
                bestBearing = bearing;
            }
        }
        return bestBearing;
    }

    private static boolean contains(int[] values, int target) {
        for (int value : values) if (value == target) return true;
        return false;
    }

    @Override
    public void close() {
        if (visionPortal != null) visionPortal.close();
    }
}
