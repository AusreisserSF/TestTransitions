package sample.auto;

import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import sample.auto.fx.FieldFXCenterStageBackdropLG;
import sample.auto.fx.RobotFXCenterStageLG;
import sample.auto.fx.CenterStageControllerLG;

public class DeviceToTargetAnimation {

    private final CenterStageControllerLG controller;
    private final Pane field;
    private final RobotFXCenterStageLG centerStageRobot;
    private final Group robotGroup;
    private final StartParameterValidation startParameters;

    private double robotCoordX;
    private double robotCoordY;
    private double deviceCenterX;
    private double deviceCenterY;
    private double aprilTagCenterX;
    private double aprilTagCenterY;

    public DeviceToTargetAnimation(CenterStageControllerLG pController, Pane pField, RobotFXCenterStageLG pCenterStageRobot,
                                   StartParameterValidation pStartParameters) {
        controller = pController;
        field = pField;
        centerStageRobot = pCenterStageRobot;
        robotGroup = centerStageRobot.getRobot();
        startParameters = pStartParameters;
    }

    public void runDeviceToTargetAnimation(RobotConstants.Alliance pAlliance, Button pPlayPauseButton) {

        //## As a demonstration start the robot facing inward from the BLUE
        // alliance wall and make the robot follow a CubicCurve path while
        // simultaneously rotating -90 degrees to face the backdrop.

        //!! I noticed the use of localToScene(() in some code from the FTCSimulator -
        // this is more like it. By the way, this is the *center* of the robot.
        Point2D loc = robotGroup.localToScene(robotGroup.getBoundsInParent().getCenterX(), robotGroup.getBoundsInParent().getCenterY());

        Path path = new Path();
        path.getElements().add(new MoveTo(loc.getX(), loc.getY()));

        //**TODO The curves are a proof-of-concept. They will be different depending
        // on the user's selection for the final position in front of the backdrop.
        // CubicCurveTo constructor parameters: controlX1, controlX2, controlY1, controlY2, endX, endY
        // The coordinates is those of the center of the robot.
        double robotPositionAtBackdropX = startParameters.getStartParameter(StartParameterValidation.StartParameter.ROBOT_POSITION_AT_BACKDROP_X) * FieldFXCenterStageBackdropLG.PX_PER_INCH;
        double robotPositionAtBackdropY = startParameters.getStartParameter(StartParameterValidation.StartParameter.ROBOT_POSITION_AT_BACKDROP_Y) * FieldFXCenterStageBackdropLG.PX_PER_INCH;
        float rotation;
        if (pAlliance == RobotConstants.Alliance.BLUE) {
            //path.getElements().add(new CubicCurveTo(400, 300, 300, 300, 200, 275));
            path.getElements().add(new CubicCurveTo(400, 300, 300, 300, robotPositionAtBackdropX, robotPositionAtBackdropY));
            rotation = -90.0f;
        } else { // RED
            //path.getElements().add(new CubicCurveTo(200, 300, 300, 300, 400, 275));
            path.getElements().add(new CubicCurveTo(200, 300, 300, 300, robotPositionAtBackdropX, robotPositionAtBackdropY));
            rotation = 90.0f;
        }

        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(3000));
        pathTransition.setPath(path);
        pathTransition.setNode(robotGroup);

        RotateTransition rotateTransition =
                new RotateTransition(Duration.millis(3000), robotGroup);
        rotateTransition.setByAngle(rotation);
        rotateTransition.setOnFinished(event -> System.out.println("Angle after initial rotation " + robotGroup.getRotate()));

        //## The TranslateTransition for the final strafe must be declared before the
        // ParallelTransition, i.e. out of the time sequence, because the distance to
        // strafe is only known after the ParallelTransition is complete.
        TranslateTransition strafeTT = new TranslateTransition(Duration.millis(2000));
        strafeTT.setNode(robotGroup);
        strafeTT.setOnFinished(event -> {
            System.out.println("After strafe layoutX " + robotGroup.getLayoutX() + ", translateX " + robotGroup.getTranslateX());
            // See answer from jewelsea in https://stackoverflow.com/questions/30338598/translatetransition-does-not-change-x-y-co-ordinates
            robotGroup.setLayoutX(robotGroup.getLayoutX() + robotGroup.getTranslateX());
            robotGroup.setLayoutY(robotGroup.getLayoutY() + robotGroup.getTranslateY());
            robotGroup.setTranslateX(0);
            robotGroup.setTranslateY(0);

            // Draw a line from the device to the AprilTag.
            Circle deviceOnRobot = (Circle) robotGroup.lookup("#" + robotGroup.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
            Point2D deviceCoord = deviceOnRobot.localToScene(deviceOnRobot.getCenterX(), deviceOnRobot.getCenterY());
            Line lineDAT = new Line(deviceCoord.getX(), deviceCoord.getY(), aprilTagCenterX, aprilTagCenterY);
            lineDAT.setId("lineDAT");
            lineDAT.setStroke(Color.YELLOW);
            lineDAT.getStrokeDashArray().addAll(10.0);
            lineDAT.setStrokeWidth(3.0);
            field.getChildren().add(lineDAT);
            System.out.println("Distance from device to AprilTag " + (aprilTagCenterY - deviceCoord.getY()));

            Bounds robotBP = robotGroup.getBoundsInParent();
            System.out.println("Robot position after strafe x " + robotBP.getCenterX() + ", y " + robotBP.getCenterY());
            //**TODO Show or at least log post-strafe position in FTC field coordinates.
        });

        RadioButton selectedRadioButton = (RadioButton) controller.approach_toggle.getSelectedToggle();
        String radioButtonText = selectedRadioButton.getText();

        //## The RotateTransition for the final rotation must be declared before the
        // ParallelTransition, i.e. out of the time sequence, because the angle to
        // rotate is only known after the ParallelTransition is complete.
        RotateTransition rotateDeviceTowardsAprilTagT = new RotateTransition(Duration.seconds(2));
        rotateDeviceTowardsAprilTagT.setNode(robotGroup);
        rotateDeviceTowardsAprilTagT.setOnFinished(event -> {
            System.out.println("Angle after rotation " + robotGroup.getRotate());

            // Draw a line from the device to the AprilTag.
            Circle deviceOnRobot = (Circle) robotGroup.lookup("#" + robotGroup.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
            Point2D deviceCoord = deviceOnRobot.localToScene(deviceOnRobot.getCenterX(), deviceOnRobot.getCenterY());
            Line lineDH = new Line(deviceCoord.getX(), deviceCoord.getY(), aprilTagCenterX, aprilTagCenterY);
            lineDH.setId("lineDCH");
            lineDH.setStroke(Color.YELLOW);
            lineDH.getStrokeDashArray().addAll(10.0);
            lineDH.setStrokeWidth(3.0);
            field.getChildren().add(lineDH);
        });

        // Follow the cubic curve and rotate in parallel.
        ParallelTransition parallelT = new ParallelTransition(pathTransition, rotateTransition);
        parallelT.setOnFinished(event -> {
            // See answer from jewelsea in https://stackoverflow.com/questions/30338598/translatetransition-does-not-change-x-y-co-ordinates
            robotGroup.setLayoutX(robotGroup.getLayoutX() + robotGroup.getTranslateX());
            robotGroup.setLayoutY(robotGroup.getLayoutY() + robotGroup.getTranslateY());
            robotGroup.setTranslateX(0);
            robotGroup.setTranslateY(0);

            Bounds robotBP = robotGroup.getBoundsInParent();
            robotCoordX = robotBP.getCenterX();
            robotCoordY = robotBP.getCenterY();

            System.out.println("Robot center in front of backdrop x " + robotCoordX + ", y " + robotCoordY);
            //**TODO Show or at least log position in FTC field coordinates?
            System.out.println("Robot rotation after positioning in front of backdrop x " + robotGroup.getRotate());

            //## All of these coordinates and calculations can only be made after
            // the ParallelTranstion is complete, i.e. now.
            Rectangle cameraOnRobot = (Rectangle) robotGroup.lookup("#" + robotGroup.getId() + "_" + RobotFXCenterStageLG.CAMERA_ON_ROBOT_ID);
            Point2D cameraCoord = cameraOnRobot.localToScene(cameraOnRobot.getX(), cameraOnRobot.getY());

            // The returned coordinates of the objects are those of the upper left-hand
            // corner. We want to draw a line from the face of the camera to the center
            // of the AprilTag.
            double cameraFaceX = cameraCoord.getX() + cameraOnRobot.getWidth() / 2;
            double cameraFaceY = cameraCoord.getY();
            System.out.println("Camera face center x " + cameraFaceX + ", y " + cameraFaceY);

            Circle deviceOnRobot = (Circle) robotGroup.lookup("#" + robotGroup.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
            Point2D deviceCoord = deviceOnRobot.localToScene(deviceOnRobot.getCenterX(), deviceOnRobot.getCenterY());
            deviceCenterX = deviceCoord.getX();
            deviceCenterY = deviceCoord.getY();
            System.out.println("Device center x " + deviceCenterX + ", y " + deviceCenterY);

            // Get the coordinates of the target AprilTag.
            Integer targetAprilTag = controller.april_tag_spinner.getValue();
            Rectangle aprilTag = (Rectangle) field.lookup("#" + FieldFXCenterStageBackdropLG.APRIL_TAG_ID + targetAprilTag);
            Point2D aprilTagCoord = aprilTag.localToScene(aprilTag.getX(), aprilTag.getY());

            aprilTagCenterX = aprilTagCoord.getX() + aprilTag.getWidth() / 2;
            aprilTagCenterY = aprilTagCoord.getY() + aprilTag.getHeight() / 2;
            System.out.println("AprilTag center x " + aprilTagCenterX + ", y " + aprilTagCenterY);

            // Get the angle from the camera to the AprilTag.
            double cameraAdjacent = Math.abs(cameraFaceY - aprilTagCenterY);
            double cameraOpposite = Math.abs(cameraFaceX - aprilTagCenterX);
            System.out.println("Camera adjacent " + cameraAdjacent + ", opposite " + cameraOpposite);

            double cameraHypotenuseSquared = Math.pow(cameraAdjacent, 2) + Math.pow(cameraOpposite, 2);
            double distanceFromCameraToAprilTag = Math.sqrt(cameraHypotenuseSquared);
            System.out.println("Distance from camera to AprilTag " + distanceFromCameraToAprilTag);

            double tanTheta = cameraOpposite / cameraAdjacent;
            double degreesFromCameraToAprilTag = Math.toDegrees(Math.atan(tanTheta));

            // Set the sign of the angle from the camera to the AprilTag: for FTC negative
            // is clockwise.
            if (aprilTagCenterX > cameraFaceX)
                degreesFromCameraToAprilTag *= -1;
            System.out.println("FTC degrees from camera to AprilTag " + degreesFromCameraToAprilTag);

            // Draw a line from the camera to the target AprilTag, the hypotenuse of the camera triangle.
            Line lineCH = new Line(cameraFaceX, cameraFaceY, aprilTagCenterX, aprilTagCenterY);
            lineCH.setId("lineCH");
            lineCH.setStroke(Color.FUCHSIA);
            lineCH.getStrokeDashArray().addAll(10.0);
            lineCH.setStrokeWidth(3.0);
            field.getChildren().add(lineCH);

            // Draw the opposite side of the camera triangle.
            Line lineCO = new Line(cameraFaceX, aprilTagCenterY, aprilTagCenterX, aprilTagCenterY);
            lineCO.setId("lineCO");
            lineCO.setStroke(Color.FUCHSIA);
            lineCO.getStrokeDashArray().addAll(10.0);
            lineCO.setStrokeWidth(3.0);
            field.getChildren().add(lineCO);

            // Draw the adjacent side of the camera triangle.
            Line lineCA = new Line(cameraFaceX, cameraFaceY, cameraFaceX, aprilTagCenterY);
            lineCA.setId("lineCA");
            lineCA.setStroke(Color.FUCHSIA);
            lineCA.getStrokeDashArray().addAll(10.0);
            lineCA.setStrokeWidth(3.0);
            field.getChildren().add(lineCA);

            // Get data about the strafe (strafe distance to position the device opposite
            // the target and distance from the device to the target) as well as the angle
            // by which the robot must be rotated so that the delivery device points at the
            // AprilTag and the final distance from the device to the AprilTag. The startup
            // parameters determine which set of data to use.

            // The fields centerStageRobot.cameraCenterFromRobotCenterPX and
            // centerStageRobot.cameraOffsetFromRobotCenterPX are already signed correctly
            // for FTC.
            // Note that cameraCenterFromRobotCenterPX measures center-to-center and so does
            // not include the distance from the camera center to its face. We need this
            // because it is part of the full distance from robot center to target center -
            // so add 1/2 of the height of the camera here.
            CameraToDeviceCorrections.CorrectionData corrections = CameraToDeviceCorrections.getCameraToDeviceCorrections(degreesFromCameraToAprilTag,
                    distanceFromCameraToAprilTag,
                    centerStageRobot.cameraCenterFromRobotCenterPX + RobotFXCenterStageLG.CAMERA_HEIGHT / 2,
                    centerStageRobot.cameraOffsetFromRobotCenterPX,
                    centerStageRobot.deviceCenterFromRobotCenterPX, centerStageRobot.deviceOffsetFromRobotCenterPX);

            // In case a strafe was selected from the start parameters.
            // Support a strafe that positions the delivery device opposite the AT.
            // Positive: strafe to the left; negative: strafe to the right. So
            // invert the FTC direction for FX.
            double finalStrafe = -corrections.strafeDistanceDeviceOppositeTarget;
            System.out.println("FX distance to strafe " + finalStrafe);
            strafeTT.setByX(finalStrafe);

            // In case a turn was selected from the start parameters.
            // The angle is correct for FTC but we need to invert for FX.
            System.out.println("Final FX turn " + -corrections.rotateRobotCenterToAlignDevice);
            rotateDeviceTowardsAprilTagT.setByAngle(-corrections.rotateRobotCenterToAlignDevice);
        });

        PauseTransition cameraToTargetPauseT = new PauseTransition(Duration.millis(2500));
        cameraToTargetPauseT.setOnFinished(event -> {
            removeCameraToTargetLines();
        });

        PauseTransition robotCenterToDevicePauseT = new PauseTransition(Duration.millis(2500));
        robotCenterToDevicePauseT.setOnFinished(event -> {
            removeCameraToTargetLines();

            // Draw the triangle formed between the center of the robot and the delivery device.
            Line lineRCDH = new Line(robotCoordX, robotCoordY, Math.abs(robotCoordX - centerStageRobot.deviceOffsetFromRobotCenterPX), aprilTagCenterY);
            lineRCDH.setId("lineRCDH");
            lineRCDH.setStroke(Color.FUCHSIA);
            lineRCDH.getStrokeDashArray().addAll(10.0);
            lineRCDH.setStrokeWidth(3.0);
            field.getChildren().add(lineRCDH);

            // Draw a line from the end point of the hypotenuse down through the center of the
            // device to the bottom of the robot. In general the device can be anywhere along
            // this line.
            Line lineDC = new Line(Math.abs(robotCoordX - centerStageRobot.deviceOffsetFromRobotCenterPX), aprilTagCenterY, Math.abs(robotCoordX - centerStageRobot.deviceOffsetFromRobotCenterPX), robotCoordX + centerStageRobot.robotHeightPX / 2);
            lineDC.setId("lineDC");
            lineDC.setStroke(Color.FUCHSIA);
            lineDC.getStrokeDashArray().addAll(10.0);
            lineDC.setStrokeWidth(3.0);
            field.getChildren().add(lineDC);

            Line lineRCDO = new Line(robotCoordX, aprilTagCenterY, Math.abs(robotCoordX - centerStageRobot.deviceOffsetFromRobotCenterPX), aprilTagCenterY);
            lineRCDO.setId("lineRCDO");
            lineRCDO.setStroke(Color.FUCHSIA);
            lineRCDO.getStrokeDashArray().addAll(10.0);
            lineRCDO.setStrokeWidth(3.0);
            field.getChildren().add(lineRCDO);

            // Draw the adjacent side of the triangle. This side is shared between
            // two triangles.
            Line lineRCA = new Line(robotCoordX, robotCoordY, robotCoordX, aprilTagCenterY);
            lineRCA.setId("lineRCA");
            lineRCA.setStroke(Color.FUCHSIA);
            lineRCA.getStrokeDashArray().addAll(10.0);
            lineRCA.setStrokeWidth(3.0);
            field.getChildren().add(lineRCA);

            // Also show the hypotenuse of the right triangle formed between the center
            // of the robot and the AprilTag.
            Line lineRCAH = new Line(robotCoordX, robotCoordY, aprilTagCenterX, aprilTagCenterY);
            lineRCAH.setId("lineRCAH");
            lineRCAH.setStroke(Color.AQUA);
            lineRCAH.getStrokeDashArray().addAll(10.0);
            lineRCAH.setStrokeWidth(3.0);
            field.getChildren().add(lineRCAH);
        });

        PauseTransition preRotationPauseT = new PauseTransition(Duration.millis(2500));
        preRotationPauseT.setOnFinished(event -> {
            // Erase the lines from robot center to device.
            Line lineRCDHRef = (Line) field.lookup("#lineRCDH");
            field.getChildren().remove(lineRCDHRef);
            Line lineDCRef = (Line) field.lookup("#lineDC");
            field.getChildren().remove(lineDCRef);
            Line lineRCDORef = (Line) field.lookup("#lineRCDO");
            field.getChildren().remove(lineRCDORef);
            Line lineRCARef = (Line) field.lookup("#lineRCA");
            field.getChildren().remove(lineRCARef);
            Line lineRCAHRef = (Line) field.lookup("#lineRCAH");
            field.getChildren().remove(lineRCAHRef);
        });

        // Look at the startup parameter that indicates whether to strafe or rotate.
        SequentialTransition seqTransition = new SequentialTransition(parallelT); // common
        if (radioButtonText.equals("Strafe to"))
            seqTransition.getChildren().addAll(cameraToTargetPauseT, strafeTT);
        else
            seqTransition.getChildren().addAll(robotCenterToDevicePauseT, preRotationPauseT, rotateDeviceTowardsAprilTagT);

        new PlayPauseToggle(pPlayPauseButton, seqTransition);
    }

    private void removeCameraToTargetLines() {
        Line lineCHRef = (Line) field.lookup("#lineCH");
        field.getChildren().remove(lineCHRef);
        Line lineCORef = (Line) field.lookup("#lineCO");
        field.getChildren().remove(lineCORef);
        Line lineCARef = (Line) field.lookup("#lineCA");
        field.getChildren().remove(lineCARef);
    }

}
