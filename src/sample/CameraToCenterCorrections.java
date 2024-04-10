package sample;

/*
Contributed by Christian Giron-Michel
Notre Dame High School
November 2022
 */
public class CameraToCenterCorrections {

    //## Replacement for FTC season 2022 and 2023 getCorrectedAngleAndDistance.
    //**TODO Extend so that this method returns the angle and distance from the
    // delivery device to the target.
    public static AngleDistance getCorrectedAngleAndDistance(double pAngleCameraToTarget, double pDistanceCameraToTarget,
                                                             double pDistanceRobotCenterToCameraFace, double pOffsetRobotCenterToCameraCenter,
                                                             double pDistanceRobotCenterToDeliveryDevice, double pOffsetRobotCenterToDeliveryDevice) {

        // We have the distance from the face of the camera to the target; this will be the hypotenuse
        // of the camera triangle. We have the angle from the camera to the target; this will be the
        // angle theta. We need to solve for the edge opposite theta and the edge adjacent to theta.
        // sine theta = opposite / hypotenuse

        // No need for trigonometry if the robot center to camera offset is 0 and
        // the angle to the target is zero.
        if (pAngleCameraToTarget == 0.0 && pOffsetRobotCenterToCameraCenter == 0.0)
            return new AngleDistance(0.0, pDistanceCameraToTarget);

        // If the angle of the camera to the target is 0 the trigonometry still works
        // because the sine of 0 is 0.
        double cameraOpposite = Math.sin(Math.toRadians(Math.abs(pAngleCameraToTarget))) * pDistanceCameraToTarget;

        // pDistanceFromCamera squared = cameraOpposite squared + cameraAdjacent squared
        double cameraAdjacentSquared = Math.pow(pDistanceCameraToTarget, 2) - Math.pow(cameraOpposite, 2);
        double cameraAdjacent = Math.sqrt(cameraAdjacentSquared);

        // Make the transformations that will yield the angle and distance to the target from the
        // center of the robot. Use the opposite and adjacent sides of the camera triangle and the
        // known position of the camera in relation to the center of the robot to calculate the
        // opposite and adjacent sides of the "robot center" triangle. Then we can get the
        // hypotenuse, which is the distance from the center of the robot to the target, and the
        // angle from the center of the robot to the target.

        // First get the opposite edge of the robot center triangle.

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

        double robotCenterOpposite = 0;
        double robotCenterSignum = Math.signum(pAngleCameraToTarget); // default
        if (pOffsetRobotCenterToCameraCenter > 0) {
            if (pAngleCameraToTarget > 0) { // target left of camera
                robotCenterOpposite = Math.abs(cameraOpposite + Math.abs(pOffsetRobotCenterToCameraCenter));
            } else if (pAngleCameraToTarget < 0) { // target right of camera
                robotCenterOpposite = Math.abs(cameraOpposite - Math.abs(pOffsetRobotCenterToCameraCenter));
                if (cameraOpposite < Math.abs(pOffsetRobotCenterToCameraCenter)) {
                    robotCenterSignum = 1;
                } else
                    robotCenterSignum = -1;
            } else { // the camera is in line with the target.
                robotCenterOpposite = Math.abs(pOffsetRobotCenterToCameraCenter);
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
            if (pAngleCameraToTarget < 0) { // target right of camera
                robotCenterOpposite = Math.abs(cameraOpposite + Math.abs(pOffsetRobotCenterToCameraCenter));
            } else if (pAngleCameraToTarget > 0) { // target left of camera
                robotCenterOpposite = Math.abs(cameraOpposite - Math.abs(pOffsetRobotCenterToCameraCenter));
                if (cameraOpposite < Math.abs(pOffsetRobotCenterToCameraCenter)) {
                    robotCenterSignum = -1;
                } else
                    robotCenterSignum = 1;
            } else { // the camera is in line with the target.
                robotCenterOpposite = Math.abs(pOffsetRobotCenterToCameraCenter);
            }
        }

        // The camera is in line with the robot center; the angle from robot center
        // to the target is the same as that of the camera, which is the default.
        // else {
        //    robotCenterSignum = Math.signum(pAngleCameraToTarget);
        // }

        // Get the "adjacent" edge of the robot center triangle.
        double robotCenterAdjacent = pDistanceRobotCenterToCameraFace + cameraAdjacent;
        System.out.println("Robot center triangle opposite " + robotCenterOpposite + ", adjacent " + robotCenterAdjacent);

        // Get the angle and distance from robot center to target center.
        double tanTheta = robotCenterOpposite / robotCenterAdjacent;
        double degreesFromRobotCenterToTarget = Math.toDegrees(Math.atan(tanTheta));
        System.out.println("Raw angle from robot center to target " + degreesFromRobotCenterToTarget);

        // Determine the FTC sign of the angle from robot center to target center.
        degreesFromRobotCenterToTarget *= robotCenterSignum;
        System.out.println("FTC angle from robot center to target " + degreesFromRobotCenterToTarget);

        double robotCenterHypotenuseSquared = Math.pow(robotCenterAdjacent, 2) + Math.pow(robotCenterOpposite, 2);
        double distanceRobotCenterToTarget = Math.sqrt(robotCenterHypotenuseSquared);
        System.out.println("Distance from robot center to target " + distanceRobotCenterToTarget);

        return new AngleDistance(degreesFromRobotCenterToTarget, distanceRobotCenterToTarget);
     }

}
