package sample;

/*
Contributed by Christian Giron-Michel
Notre Dame High School
November 2022
 */
public class CameraToCenterCorrections {

    public static final double pi = Math.PI;

    //The formulas change depending on whether we have the camera positioned to the left or to the right of the center of the robot.
    public enum CameraPosition {
        LEFT, CENTER, RIGHT
    }

    private enum CameraForeAft {
        FORE, CENTER, AFT
    }

    private enum CameraOffset {
        LEFT, CENTER, RIGHT
    }

    //**TODO EXPERIMENTAL replacement
    public static AngleDistance getCorrectedAngleAndDistance2(double pAngleFromCamera, double pDistanceFromCamera,
         double pDistanceRobotCenterToCameraFace, double pOffsetRobotCenterToCamera) {

        // Gets the fore/aft position of the camera in relation to the center of the robot
        // when observed from behind.
        CameraForeAft cameraForeAft = pDistanceRobotCenterToCameraFace < 0 ? CameraForeAft.AFT :
                pOffsetRobotCenterToCamera > 0 ? CameraForeAft.FORE : CameraForeAft.CENTER;

        // Gets the left/right position of the camera in relation to the center of the robot
        // when observed from behind.
        CameraOffset cameraOffset = pOffsetRobotCenterToCamera < 0 ? CameraOffset.RIGHT :
                pOffsetRobotCenterToCamera > 0 ? CameraOffset.LEFT : CameraOffset.CENTER;

        // We have the distance from the face of the camera to the target; this will be the hypotenuse
        // of the camera triangle. We have the angle from the camera to the target; this will be the
        // angle theta. We need to solve for the edge opposite theta and the edge adjacent to theta.
        // sine theta = opposite / hypotenuse
        double cameraOpposite = Math.sin(Math.toRadians(Math.abs(pAngleFromCamera))) * pDistanceFromCamera;

        // pDistanceFromCamera squared = cameraOpposite squared + cameraAdjacent squared
        double cameraAdjacentSquared = Math.pow(pDistanceFromCamera, 2) - Math.pow(cameraOpposite, 2);
        double cameraAdjacent = Math.sqrt(cameraAdjacentSquared);

        // Make the transformations that will yield the angle and distance to the target from the
        // center of the robot. Use the opposite and adjacent sides of the camera triangle and the
        // known position of the camera in relation to the center of the robot to calculate the
        // opposite and adjacent sides of the "robot center" triangle. Then we can get the
        // hypotenuse, which is the distance from the center of the robot to the target, and the
        // angle from the center of the robot to the target.

        // First get the opposite edge of the robot center triangle.
        //**TODO ??I don't think addition works for all cases ... only if the target
        // is to the right of the camera ?? and the camera is to the right of the robot center??
        double robotCenterOpposite = Math.abs(Math.abs(pOffsetRobotCenterToCamera) + cameraOpposite);

        // Then get the adjacent edge of the robot center triangle.
        double robotCenterAdjacent = pDistanceRobotCenterToCameraFace + cameraAdjacent;
        System.out.println("Robot center triangle opposite " + robotCenterOpposite + ", adjacent " + robotCenterAdjacent);

        // Get the angle and distance from robot center to target center.
        double tanTheta = robotCenterOpposite / robotCenterAdjacent;
        double degreesFromRobotCenterToTarget = Math.toDegrees(Math.atan(tanTheta));
        System.out.println("Raw angle from robot center to target " + degreesFromRobotCenterToTarget);

        // Determine the FTC sign of the angle from robot center to target center.
        // The FTC sign is the same as the signum.
        double robotCenterSignum = Math.signum(Math.abs(pOffsetRobotCenterToCamera) - cameraOpposite);
        degreesFromRobotCenterToTarget *= robotCenterSignum;
        System.out.println("FTC angle from robot center to target " + degreesFromRobotCenterToTarget);

        double robotCenterHypotenuseSquared = Math.pow(robotCenterAdjacent, 2) + Math.pow(robotCenterOpposite, 2);
        double distanceRobotCenterToTarget = Math.sqrt(robotCenterHypotenuseSquared);
        System.out.println("Distance from robot center to target " + distanceRobotCenterToTarget);

        return new AngleDistance(degreesFromRobotCenterToTarget, distanceRobotCenterToTarget);
     }

    // This method returns both the angle and distance.
    public static AngleDistance getCorrectedAngleAndDistance(double distanceFromCenterToFront, double offset, double distanceFromCamera, double angleFromCamera) {
        double correctedAngle = getCorrectedAngle(distanceFromCenterToFront, offset, distanceFromCamera, angleFromCamera);
        double correctedDistance = getCorrectedDistance(distanceFromCenterToFront, offset, distanceFromCamera, angleFromCamera);
        return new AngleDistance(correctedAngle, correctedDistance);
    }

    //Computes the distance from the center of the robot to the object using a formula derived from the law of cosines.
    private static double getCorrectedDistance(double distanceFromCenterToFront, double offset, double distanceFromCamera, double angleFromCamera) {

        //Converts the input angle from degrees to radians.
        angleFromCamera = Math.toRadians(angleFromCamera);

        //Returns the direction of the camera offset from the center of the robot when looking from behind.
        CameraPosition cameraPosition;
        if (offset < 0) {
            cameraPosition = CameraPosition.RIGHT;
        } else if (offset > 0) {
            cameraPosition = CameraPosition.LEFT;
        } else {
            cameraPosition = CameraPosition.CENTER;
        }

        //Gets the magnitude of the horizontal offset to ensure that angles stay positive.
        offset = Math.abs(offset);

        //Measure in inches the distance from the camera to the center of the robot.
        //Always positive.
        double distanceFromCenterToCamera = Math.sqrt(Math.pow(distanceFromCenterToFront, 2) + Math.pow(offset, 2));

        //Measure in degrees from the heading of the camera to a line that connects the camera & the center of the robot.
        //Always positive & less than 180 degrees.
        double angleFromCameraToCenter = Math.toRadians(90) + Math.atan(distanceFromCenterToFront / offset);

        //The sum of the squares of the distance between the center of the robot & the camera and the distance between the camera & the object.
        double pythSum = Math.pow(distanceFromCenterToCamera, 2) + Math.pow(distanceFromCamera, 2);

        switch (cameraPosition) {
            case LEFT -> {
                //Returns the distance from the center of the robot to the object.
                return Math.sqrt(pythSum - 2 * distanceFromCamera * distanceFromCenterToCamera * Math.cos(angleFromCameraToCenter + angleFromCamera));
            }
            case CENTER -> {
                //Returns the distance from the center of the robot to the object.
                return Math.sqrt(pythSum - 2 * distanceFromCamera * distanceFromCenterToCamera * Math.cos(pi - angleFromCamera));
            }
            case RIGHT -> {
                //Returns the distance from the center of the robot to the object.
                return Math.sqrt(pythSum - 2 * distanceFromCamera * distanceFromCenterToCamera * Math.cos(angleFromCameraToCenter - angleFromCamera));
            }
        }

        return 0;

    }

    //Computes the angle from the heading of the robot to the object using a formula derived from the law of sines.
    //Returns a positive angle if the robot needs to turn counterclockwise.
    //Returns a negative angle if the robot needs to turn clockwise.
    private static double getCorrectedAngle(double distanceFromCenterToFront, double offset, double distanceFromCamera, double angleFromCamera) {

        //Converts the input angle from degrees to radians.
        angleFromCamera = Math.toRadians(angleFromCamera);

        //Returns the direction of the camera offset from the center of the robot when looking from behind.
        CameraPosition cameraPosition;
        if (offset < 0) {
            cameraPosition = CameraPosition.RIGHT;
        } else if (offset > 0) {
            cameraPosition = CameraPosition.LEFT;
        } else {
            cameraPosition = CameraPosition.CENTER;
        }

        //Gets the magnitude of the horizontal offset to ensure that angles stay positive.
        offset = Math.abs(offset);

        //Measure in degrees from the heading of the camera to a line that connects the camera & the center of the robot.
        //Always positive & less than 180 degrees.
        double angleFromCameraToCenter = Math.toRadians(90) + Math.atan(distanceFromCenterToFront / offset);
        //Measure in degrees from the heading of the center of the robot (the robot itself) to the line that connects the camera & the center of the robot.
        //Supplement to "angleFromCameraToCenter"
        double angleFromCenterToCamera = pi - angleFromCameraToCenter;

        switch (cameraPosition) {
            case LEFT -> {
                //Computes the distance from the center of the robot to the object using the distance function defined above.
                double distanceFromCenter = getCorrectedDistance(distanceFromCenterToFront, offset, distanceFromCamera, Math.toDegrees(angleFromCamera));
                //Computes the sine of the angle between the line connecting the camera & the object and the line connecting the center of the robot to the camera.
                double sinOppositeCenterLine = Math.sin(angleFromCameraToCenter + angleFromCamera);
                //Computes the angle between the line connecting the center of the robot to the camera & the line connecting the center of the robot to the object.
                double theta = Math.asin((distanceFromCamera * sinOppositeCenterLine) / distanceFromCenter);
                //Returns the angle between the heading of the robot & the line connecting the center of the robot to the object.
                return Math.toDegrees(angleFromCenterToCamera - theta);

            }
            case CENTER -> {
                //Computes the distance from the center of the robot to the object using the distance function defined above.
                double distanceFromCenter = getCorrectedDistance(distanceFromCenterToFront, offset, distanceFromCamera, Math.toDegrees(angleFromCamera));
                //Computes the sine of the angle between the line connecting the camera & the object and the line connecting the center of the robot to the camera.
                double sinOppositeCenterLine = Math.sin(pi - angleFromCamera);
                //Computes the angle between the line connecting the center of the robot to the camera & the line connecting the center of the robot to the object.
                double theta = Math.asin((distanceFromCamera * sinOppositeCenterLine) / distanceFromCenter);
                //Returns the angle between the heading of the robot & the line connecting the center of the robot to the object.
                return Math.toDegrees(theta);
            }
            case RIGHT -> {
                //Computes the distance from the center of the robot to the object using the distance function defined above.
                double distanceFromCenter = getCorrectedDistance(distanceFromCenterToFront, offset, distanceFromCamera, Math.toDegrees(angleFromCamera));
                //Computes the sine of the angle between the line connecting the camera & the object and the line connecting the center of the robot to the camera.
                double sinOppositeCenterLine = Math.sin(angleFromCameraToCenter - angleFromCamera);
                //Computes the angle between the line connecting the center of the robot to the camera & the line connecting the center of the robot to the object.
                double theta = Math.asin((distanceFromCamera * sinOppositeCenterLine) / distanceFromCenter);
                //Returns the angle between the heading of the robot & the line connecting the center of the robot to the object.
                return Math.toDegrees(-(angleFromCenterToCamera - theta));
            }
        }

        return 0;
    }

}
