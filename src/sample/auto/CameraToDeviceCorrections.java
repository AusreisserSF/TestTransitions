package sample.auto;

/*
Based on the orginal contribution from Christian Giron-Michel
Notre Dame High School
November 2022
 */

public class CameraToDeviceCorrections {

    // There are four triangles involved in the animation of the robot: camera to
    // target, robot center to target, robot center to device pre-rotation, and
    // device to target post-rotation. Note that the adjacent sides of the robot
    // center to target triangle and the robot center to device triangle
    // pre-rotation are the same.

    //**TODO Need diagrams of all of the triangles. Try https://app.diagrams.net/.

    public static CorrectionData getCameraToDeviceCorrections(double pAngleCameraFaceToTarget, double pDistanceCameraFaceToTarget,
                                                              double pDistanceRobotCenterToCameraFace, double pOffsetRobotCenterToCameraCenter,
                                                              double pDistanceRobotCenterToDeliveryDevice, double pOffsetRobotCenterToDeliveryDevice) {

        // If the angle of the camera to the target is 0 the trigonometry still works
        // because the sine of 0 is 0.
        double cameraOpposite = Math.sin(Math.toRadians(Math.abs(pAngleCameraFaceToTarget))) * pDistanceCameraFaceToTarget;

        // pDistanceFromCamera squared = cameraOpposite squared + cameraAdjacent squared
        double cameraAdjacentSquared = Math.pow(pDistanceCameraFaceToTarget, 2) - Math.pow(cameraOpposite, 2);
        double cameraAdjacent = Math.sqrt(cameraAdjacentSquared);

        // Make the transformations that will yield the angle and distance from the
        // center of the robot to the target. Use the opposite and adjacent sides of
        // the camera triangle and the known position of the camera in relation to
        // the center of the robot to calculate the opposite and adjacent sides of
        // the "robot center to target" triangle. Then we can get the hypotenuse,
        // which is the distance from the center of the robot to the target, and the
        // angle from the center of the robot to the target.

        // First get the opposite edge of the robot center to target triangle.

        // If the camera is left of robot center (the offset is positive)
        //  If the target is left of the camera (the angle is positive)
        //    the robot center "opposite" extends to the left.
        //  Else if the target is right of the camera (the angle is negative)
        //    reduce the robot center "opposite".
        //  Else
        //    the camera is in line with the target.

        // Also set the sign of the angle from the robot center to the target.
        // If the camera is left of robot center (the offset is positive)
        // AND the target is right of the camera (the angle is negative)
        // AND the target is left of robot center (camera "opposite" < abs(offset))
        // then the angle from robot center to the target is positive.

        // This is the robot center to target triangle.
        double absOffsetRobotCenterToCameraCenter = Math.abs(pOffsetRobotCenterToCameraCenter); // needed multiple places
        double rctOpposite = 0;
        double rctSignum = Math.signum(pAngleCameraFaceToTarget); // default
        if (pOffsetRobotCenterToCameraCenter > 0) {
            if (pAngleCameraFaceToTarget > 0) { // target left of camera
                rctOpposite = Math.abs(cameraOpposite + absOffsetRobotCenterToCameraCenter);
            } else if (pAngleCameraFaceToTarget < 0) { // target right of camera
                rctOpposite = Math.abs(cameraOpposite - absOffsetRobotCenterToCameraCenter);
                if (cameraOpposite < Math.abs(pOffsetRobotCenterToCameraCenter)) {
                    rctSignum = 1;
                } else
                    rctSignum = -1;
            } else { // the camera is in line with the target.
                rctOpposite = Math.abs(pOffsetRobotCenterToCameraCenter);
            }
        }

        // If the camera is right of robot center (the offset is negative)
        //  If the target is right of the camera (the angle is negative)
        //    the robot center "opposite" extends to the right.
        //  Else if the target is left of the camera (the angle is positive)
        //    reduce the robot center "opposite".
        //  Else
        //    the camera is in line with the target.

        // Also set the sign of the angle from the robot center to the target.
        // If the camera is right of robot center (the offset is negative)
        // AND the target is left of the camera (the angle is positive)
        // AND the target is right of robot center (camera "opposite" < abs(offset))
        // then the angle from robot center to the target is negative.
        if (pOffsetRobotCenterToCameraCenter < 0) {
            if (pAngleCameraFaceToTarget < 0) { // target right of camera
                rctOpposite = Math.abs(cameraOpposite + Math.abs(pOffsetRobotCenterToCameraCenter));
            } else if (pAngleCameraFaceToTarget > 0) { // target left of camera
                rctOpposite = Math.abs(cameraOpposite - Math.abs(pOffsetRobotCenterToCameraCenter));
                if (cameraOpposite < Math.abs(pOffsetRobotCenterToCameraCenter)) {
                    rctSignum = -1;
                } else
                    rctSignum = 1;
            } else { // the camera is in line with the target.
                rctOpposite = Math.abs(pOffsetRobotCenterToCameraCenter);
            }
        }

        // The camera is in line with the robot center; the angle and distance
        // from robot center to target are the same as those of the camera.
        if (pOffsetRobotCenterToCameraCenter == 0.0) {
            rctSignum = Math.signum(pAngleCameraFaceToTarget);
            rctOpposite = cameraOpposite;
        }

        // Get the "adjacent" edge of the robot center triangle.
        double rctAdjacent = pDistanceRobotCenterToCameraFace + cameraAdjacent;
        System.out.println("Robot center triangle opposite " + rctOpposite + ", adjacent " + rctAdjacent);

        // Get the angle and distance from robot center to target center.
        double tanTheta = rctOpposite / rctAdjacent;
        double rctAngle = Math.toDegrees(Math.atan(tanTheta));
        System.out.println("Raw angle from robot center to target " + rctAngle);

        // Determine the FTC sign of the angle from robot center to target center.
        rctAngle *= rctSignum;
        System.out.println("FTC angle from robot center to target " + rctAngle);

        double rctHypotenuseSquared = Math.pow(rctAdjacent, 2) + Math.pow(rctOpposite, 2);
        double rctDistance = Math.sqrt(rctHypotenuseSquared);
        System.out.println("Distance from robot center to target " + rctDistance);

        // Short circuit in the unlikely case that the device is positioned exactly at robot center.
        if (pDistanceRobotCenterToDeliveryDevice == 0.0 && pOffsetRobotCenterToDeliveryDevice == 0.0)
            return new CorrectionData(rctOpposite, rctDistance, rctAngle, rctDistance);

        // We have the right triangle with a hypotenuse from the center of the robot to
        // the target. Now we need the right triangle with a hypotenuse from the center
        // of the robot to the point where a vertical line from the delivery device
        // forms a right angle with the horizontal line from the center of the target.
        // The adjacent side of this triangle is the same as the adjacent side of the
        // robot center to target triangle.
        double rcdPreRotationAdjacentSquared = Math.pow(rctDistance, 2) - Math.pow(rctOpposite, 2);
        double rcdPreRotationAdjacent = Math.sqrt(rcdPreRotationAdjacentSquared);

        // The opposite side of the triangle is the same as the offset from the center
        // of the robot to the center of the device.
        double rcdPreRotationOpposite = Math.abs(pOffsetRobotCenterToDeliveryDevice);
        double rcdPreRotationHypotenuseSquared = Math.pow(rcdPreRotationAdjacent, 2) + Math.pow(rcdPreRotationOpposite, 2);
        double rdcPreRotationHypotenuse = Math.sqrt(rcdPreRotationHypotenuseSquared);

        // Get the angle at the center of the robot given the triangle defined above.
        double rcdPreRotationSin = rcdPreRotationOpposite / rdcPreRotationHypotenuse;
        double rdcPreRotationAngle = Math.toDegrees(Math.asin(rcdPreRotationSin));
        System.out.println("Angle from robot center to right angle between device and target " + rdcPreRotationAngle);

        // Set the angle to rotate so that the device is facing the target. Use
        // the FTC convention: positive angle for a CCW turn, negative for CW.
        double finalTurnFromRobotCenter;

        // The angle rcdPreRotationAngle is always zero or positive.
        // If the device is to the right of robot center then invert
        // the sign of the angle.
        if (pOffsetRobotCenterToDeliveryDevice < 0.0)
            rdcPreRotationAngle *= -1;

        // Now determine the final FTC sign of the angle to turn the robot so
        // that the device faces the target. Along the way gather information
        // to use in determining the distance and sign of the strafe that will
        // place the device opposite the target.
        double strafeDistance = rctOpposite; // start with robot center to target distance

        // If the signs of the FTC angles from robot center to device and
        // robot center to target are not the same then for the final turn
        // add their absolute values. The robot has to turn further.
        if (Math.signum(rdcPreRotationAngle) != Math.signum(rctAngle)) {
            strafeDistance += rcdPreRotationOpposite;
            finalTurnFromRobotCenter = Math.abs(rdcPreRotationAngle) + Math.abs(rctAngle);
        }

        // The signs of the FTC angles from robot center to device and
        // device to target are the same so the angles overlap; for the
        // final turn take the absolute value of their difference.
        else {
            strafeDistance = Math.abs(strafeDistance - rcdPreRotationOpposite);
            finalTurnFromRobotCenter = Math.abs(Math.abs(rdcPreRotationAngle) - Math.abs(rctAngle));
        }

        // The FTC sign of the final turn is the inverse of the sign of the angle
        // from device to target.
        if (rctAngle > 0) { // target is left of robot center
            if (pOffsetRobotCenterToDeliveryDevice < 0) {
                finalTurnFromRobotCenter = Math.abs(finalTurnFromRobotCenter); // device is right of robot center, turn FTC CCW
            } else // pOffsetRobotCenterToDeliveryDevice >= 0
                // Device is left of robot center; is the device still right of the target?
                if (Math.abs(pOffsetRobotCenterToDeliveryDevice) > rctOpposite) {
                    finalTurnFromRobotCenter = Math.abs(finalTurnFromRobotCenter); // device is left of target, FTC turn CCW
                }
        }

        if (rctAngle < 0) { // target is right of robot center
            if (pOffsetRobotCenterToDeliveryDevice > 0) {
                finalTurnFromRobotCenter = -Math.abs(finalTurnFromRobotCenter); // device is left of robot center, turn FTC CW
            } else // pOffsetRobotCenterToDeliveryDevice >= 0
                // Device is right of robot center; is the device still left of the target?
                if (Math.abs(pOffsetRobotCenterToDeliveryDevice) <= rctOpposite) {
                    finalTurnFromRobotCenter = -Math.abs(finalTurnFromRobotCenter); // device is left of target, FTC turn CW
                }
        }

        // The FTC sign of the strafe is the same as that of the angle between
        // robot center and target.
        strafeDistance *= Math.signum(finalTurnFromRobotCenter);

        // After the strafe is complete we need to know the distance from the device
        // to the target.
        double postStrafeDistanceDeviceToTarget = rcdPreRotationAdjacent - pDistanceRobotCenterToDeliveryDevice;

        // Calculate the post-rotation distance from the device to target.
        // For this we need the triangle formed post-rotation from robot
        // center to device. The hypotenuse of this triangle is the line from
        // robot center to target - this line does not change as the robot rotates.
        // The right angle of the triangle is formed by a line from robot center
        // that intersects a line from device to target. The length of the first
        // line is the same as the absolute value of the offset of the device from
        // robot center. The second line forms the basis of the final distance from
        // device to target.
        // rctDistance squared = pOffsetRobotCenterToDeliveryDevice squared + device
        // to target distance (dtDistance) squared.
        double dtDistancePostRotationSquared = Math.abs(Math.pow(rctDistance, 2) - Math.pow(pOffsetRobotCenterToDeliveryDevice, 2));
        double dtPostRotationDistance = Math.sqrt(dtDistancePostRotationSquared);

        // We're not quite done: the device may be either closer to the target than
        // robot center or further away. So we need an adjustment.
        dtPostRotationDistance -= pDistanceRobotCenterToDeliveryDevice; // negative increases the distance
        System.out.println("Post-rotation distance from device to target " + dtPostRotationDistance);

        return new CorrectionData(strafeDistance, postStrafeDistanceDeviceToTarget, finalTurnFromRobotCenter, dtPostRotationDistance);
    }

    public static class CorrectionData {
        public final double strafeDistanceDeviceOppositeTarget; // FTC: positive to the left, negative to the right
        public final double postStrafeDistanceDeviceToTarget;

        // Amount by which to rotate the robot about its center so that the device
        // points at the target.
        public final double rotateRobotCenterToAlignDevice;

        // Distance from the device to the target after rotation.
        public final double postRotationDeviceToTargetDistance;

        public CorrectionData(double pStrafeDistanceDeviceOppositeTarget, double pPostStrafeDistanceDeviceToTarget,
                              double pRotateRobotCenterToAlignDevice, double pPostRotationDeviceToTargetDistance) {
            strafeDistanceDeviceOppositeTarget = pStrafeDistanceDeviceOppositeTarget;
            postStrafeDistanceDeviceToTarget = pPostStrafeDistanceDeviceToTarget;
            rotateRobotCenterToAlignDevice = pRotateRobotCenterToAlignDevice;
            postRotationDeviceToTargetDistance = pPostRotationDeviceToTargetDistance;
        }
    }

}
