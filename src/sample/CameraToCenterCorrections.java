package sample;

/*
Contributed by Christian Giron-Michel
Notre Dame High School
November 2022
 */
public class CameraToCenterCorrections {

    //## Replacement for FTC season 2022 and 2023 getCorrectedAngleAndDistance
    public static AngleDistance getCorrectedAngleAndDistance(double pAngleCameraToTarget, double pDistanceCameraToTarget,
                                                             double pDistanceRobotCenterToCameraFace, double pOffsetRobotCenterToCameraCenter) {

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
        //**TODO BROKEN - does not work when the camera is left of center ...
        double angleRobotCenterToTargetSignum = 0;
        // If the target is to the *right* of the camera (the angle is negative) and the
        // camera is to the *right* of robot center (offset is negative) ...
        double robotCenterOpposite = pAngleCameraToTarget < 0 ? Math.abs(cameraOpposite + Math.abs(pOffsetRobotCenterToCameraCenter)) :
                Math.abs(cameraOpposite - Math.abs(pOffsetRobotCenterToCameraCenter));

        // If the target is to the *right* of the camera (the angle is negative) and the
        // camera is to the *left* of robot center (offset is positive) ...
        // then the calculations are the opposite of the above.

        // If the target is to the *right* of the camera (the angle is negative) and the
        // camera is *directly opposite* robot center (offset is 0) ...
        // robot center opposite == camera opposite (math above works)

        // If the target is to the *left* of the camera (the angle is positive) and the
        // camera is to the *left* of robot center (offset is positive) ...
        // same as above

        // If the target is to the *left* of the camera (the angle is positive) and the
        // camera is to the *right* of robot center (offset is negative) ...
        // then the calculations are the opposite of the above.

        // If the target is to the *left* of the camera (the angle is positive) and the
        // camera is *directly opposite* robot center (offset is 0) ...
        // robot center opposite == camera opposite (math above works)

        // If the target is *directly opposite* the camera and the camera is *right*
        // of robot center then ...

        // If the target is *directly opposite* the camera and the camera is *left*
        // of robot center then ...

        // Then get the adjacent edge of the robot center triangle.
        double robotCenterAdjacent = pDistanceRobotCenterToCameraFace + cameraAdjacent;
        System.out.println("Robot center triangle opposite " + robotCenterOpposite + ", adjacent " + robotCenterAdjacent);

        // Get the angle and distance from robot center to target center.
        double tanTheta = robotCenterOpposite / robotCenterAdjacent;
        double degreesFromRobotCenterToTarget = Math.toDegrees(Math.atan(tanTheta));
        System.out.println("Raw angle from robot center to target " + degreesFromRobotCenterToTarget);

        // Determine the FTC sign of the angle from robot center to target center.
        //**TODO BROKEN - the only surefire way to know the direction of the angle is
        // to compare the x position of the robot with that of the target ...
        double robotCenterSignum = cameraOpposite < Math.abs(pDistanceRobotCenterToCameraFace) ? -1 : 1;
        degreesFromRobotCenterToTarget *= robotCenterSignum;
        System.out.println("FTC angle from robot center to target " + degreesFromRobotCenterToTarget);

        double robotCenterHypotenuseSquared = Math.pow(robotCenterAdjacent, 2) + Math.pow(robotCenterOpposite, 2);
        double distanceRobotCenterToTarget = Math.sqrt(robotCenterHypotenuseSquared);
        System.out.println("Distance from robot center to target " + distanceRobotCenterToTarget);

        return new AngleDistance(degreesFromRobotCenterToTarget, distanceRobotCenterToTarget);
     }

}
