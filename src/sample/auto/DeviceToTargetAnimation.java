package sample.auto;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import org.firstinspires.ftc.ftcdevcommon.AutonomousRobotException;
import sample.auto.fx.FieldFXCenterStageBackdropLG;
import sample.auto.fx.RobotFXCenterStageLG;
import sample.auto.fx.CenterStageControllerLG;

public class DeviceToTargetAnimation {

    private final String TAG = DeviceToTargetAnimation.class.getSimpleName();
    private final RobotConstants.Alliance alliance;
    private final CenterStageControllerLG controller;
    private final Pane field;
    private final RobotFXCenterStageLG previewRobot;
    private final RobotFXCenterStageLG animationRobot;
    private final Group animationRobotGroup;

    private double robotCoordX;
    private double robotCoordY;
    private double deviceCenterX;
    private double deviceCenterY;
    private double aprilTagCenterX;
    private double aprilTagCenterY;
    private CameraToDeviceCorrections.CorrectionData corrections;

    public DeviceToTargetAnimation(RobotConstants.Alliance pAlliance,
                                   CenterStageControllerLG pController, Pane pField,
                                   RobotFXCenterStageLG pPreviewRobot, RobotFXCenterStageLG pAnimationRobot) {
        alliance = pAlliance;
        controller = pController;
        field = pField;
        animationRobot = pAnimationRobot;
        previewRobot = pPreviewRobot;
        animationRobotGroup = animationRobot.getRobot();
    }

    //**TODO Clarify flow of control; separate onFinished events?
    public void runDeviceToTargetAnimation(Button pPlayPauseButton) {

        //## As a demonstration start the robot facing inward from the BLUE
        // alliance wall and make the robot follow a CubicCurve pathToBackdrop while
        // simultaneously rotating -90 degrees to face the backdrop.

        //!! I noticed the use of localToScene(() in some code from the FTCSimulator -
        // this is more like it. By the way, this is the *center* of the robot.
        Point2D animationRobotLocation = animationRobotGroup.localToScene(animationRobotGroup.getBoundsInParent().getCenterX(), animationRobotGroup.getBoundsInParent().getCenterY());

        // A slight pause after the preview and before the animation starts.
        PauseTransition postPreviewPauseT = new PauseTransition(Duration.millis(500));

        // It would be clearer if we could define the paths from the animation robot's
        // blue or red start position to the robot's position in fron t of the backdrop.
        // But the Preview is still in effect and the user may drag and release the
        // preview robot. So we have to wait until the user hits the Play button to
        // capture the preview robot's final position in front of the backdrop so that
        // we can use that position for the animation robot.
        Path pathToBackdrop = new Path();
        pathToBackdrop.getElements().add(new MoveTo(animationRobotLocation.getX(), animationRobotLocation.getY()));
        CubicCurveTo cubicCurveTo;
        float rotation;
        if (alliance == RobotConstants.Alliance.BLUE) {
            cubicCurveTo = new CubicCurveTo(400.0, 300.0, 300.0, 300.0, 0, 0);
            rotation = -90.0f;
        } else { // RED
            cubicCurveTo = new CubicCurveTo(200.0, 300.0, 300.0, 300.0, 0, 0);
            rotation = 90.0f;
        }

        pathToBackdrop.getElements().add(cubicCurveTo);

        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(3000));
        pathTransition.setPath(pathToBackdrop);
        pathTransition.setNode(animationRobotGroup);

        RotateTransition rotateTransition =
                new RotateTransition(Duration.millis(3000), animationRobotGroup);
        rotateTransition.setByAngle(rotation);
        rotateTransition.setOnFinished(event -> System.out.println("Angle after initial rotation " + animationRobotGroup.getRotate()));

        //## The TranslateTransition for the final strafe must be declared before the
        // ParallelTransition, i.e. out of the time sequence, because the distance to
        // strafe is only known after the ParallelTransition is complete.
        TranslateTransition strafeTT = new TranslateTransition(Duration.millis(2000));
        strafeTT.setNode(animationRobotGroup);
        strafeTT.setOnFinished(event -> {
            System.out.println("After strafe layoutX " + animationRobotGroup.getLayoutX() + ", translateX " + animationRobotGroup.getTranslateX());
            // See answer from jewelsea in https://stackoverflow.com/questions/30338598/translatetransition-does-not-change-x-y-co-ordinates
            animationRobotGroup.setLayoutX(animationRobotGroup.getLayoutX() + animationRobotGroup.getTranslateX());
            animationRobotGroup.setLayoutY(animationRobotGroup.getLayoutY() + animationRobotGroup.getTranslateY());
            animationRobotGroup.setTranslateX(0);
            animationRobotGroup.setTranslateY(0);

            // Draw a line from the device to the target AprilTag.
            Circle deviceOnRobot = (Circle) animationRobotGroup.lookup("#" + animationRobotGroup.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
            Point2D deviceCoord = deviceOnRobot.localToScene(deviceOnRobot.getCenterX(), deviceOnRobot.getCenterY());
            Line lineDT = new Line(deviceCoord.getX(), deviceCoord.getY(), aprilTagCenterX, aprilTagCenterY);
            lineDT.setId("lineDT");
            lineDT.setStroke(Color.YELLOW);
            lineDT.getStrokeDashArray().addAll(10.0);
            lineDT.setStrokeWidth(3.0);
            field.getChildren().add(lineDT);
            System.out.println("Distance from device to AprilTag " + (aprilTagCenterY - deviceCoord.getY()));

            Bounds robotBP = animationRobotGroup.getBoundsInParent();
            System.out.println("Robot position after strafe x " + robotBP.getCenterX() + ", y " + robotBP.getCenterY());
        });

        RadioButton selectedRadioButton = (RadioButton) controller.approach_toggle.getSelectedToggle();
        String radioButtonText = selectedRadioButton.getText();

        //## The RotateTransition for the final rotation of the robot so that the
        // device lines up with the target must be declared before the
        // ParallelTransition, i.e. out of the time sequence, because the angle to
        // rotate is only known after the ParallelTransition is complete.
        RotateTransition rotateDeviceTowardsAprilTagT = new RotateTransition(Duration.seconds(2));
        rotateDeviceTowardsAprilTagT.setNode(animationRobotGroup);
        rotateDeviceTowardsAprilTagT.setOnFinished(event -> {
            System.out.println("Angle after rotation " + animationRobotGroup.getRotate());

            // Draw a line from the device to the target AprilTag.
            Circle deviceOnRobot = (Circle) animationRobotGroup.lookup("#" + animationRobotGroup.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
            Point2D deviceCoord = deviceOnRobot.localToScene(deviceOnRobot.getCenterX(), deviceOnRobot.getCenterY());
            Line lineDT = new Line(deviceCoord.getX(), deviceCoord.getY(), aprilTagCenterX, aprilTagCenterY);
            lineDT.setId("lineDCH");
            lineDT.setStroke(Color.YELLOW);
            lineDT.getStrokeDashArray().addAll(10.0);
            lineDT.setStrokeWidth(3.0);
            field.getChildren().add(lineDT);
        });

        // If we're treating the device as a turret then the robot itself does
        // not need to turn. Just draw the lines from the turret to the target.
        PauseTransition turretToTargetPauseT = new PauseTransition(Duration.millis(2500));
        turretToTargetPauseT.setOnFinished(event -> {
            removeCameraToTargetLines();

            // Use information about the strafe distances to calculate the angle
            // and distances for the turret.

            // The opposite side of the turret to target triangle is the same
            // as the strafe distance from the device to the target.
            double distanceTurretToTargetOpposite = Math.abs(corrections.strafeDistanceDeviceOppositeTarget);

            // The adjacent side of the turret to target triangle is the same
            // as the final post-strafe distance from the device from the target.
            double distanceTurretToTargetAdjacent = corrections.postStrafeDistanceDeviceToTarget;

            // Now we can calculate the hypotenuse and the angle.
            double ttHypotenuseSquared = Math.pow(distanceTurretToTargetOpposite, 2) + Math.pow(distanceTurretToTargetAdjacent, 2);
            double ttHypotenuse = Math.sqrt(ttHypotenuseSquared);
            System.out.println("Distance from turret to target" + ttHypotenuse);

            // sin theta = opposite / hypotenuse
            double sinTTAngle = distanceTurretToTargetOpposite / ttHypotenuse;
            double ttAngle = Math.toDegrees(Math.asin(sinTTAngle));
            System.out.println("Angle from turret to target" + ttAngle);

            // Draw the hypotenuse of the device (turret) to target (AprilTag) triangle.
            Line lineTTH = new Line(deviceCenterX, deviceCenterY, aprilTagCenterX, aprilTagCenterY);
            lineTTH.setStroke(Color.YELLOW);
            lineTTH.getStrokeDashArray().addAll(10.0);
            lineTTH.setStrokeWidth(3.0);
            field.getChildren().add(lineTTH);
        });

        // Follow the cubic curve and rotate in parallel.
        ParallelTransition parallelT = new ParallelTransition(pathTransition, rotateTransition);
        parallelT.setOnFinished(event -> {
            // See answer from jewelsea in https://stackoverflow.com/questions/30338598/translatetransition-does-not-change-x-y-co-ordinates
            animationRobotGroup.setLayoutX(animationRobotGroup.getLayoutX() + animationRobotGroup.getTranslateX());
            animationRobotGroup.setLayoutY(animationRobotGroup.getLayoutY() + animationRobotGroup.getTranslateY());
            animationRobotGroup.setTranslateX(0);
            animationRobotGroup.setTranslateY(0);

            Bounds robotBP = animationRobotGroup.getBoundsInParent();
            robotCoordX = robotBP.getCenterX();
            robotCoordY = robotBP.getCenterY();

            System.out.println("Robot center in front of backdrop x " + robotCoordX + ", y " + robotCoordY);
            System.out.println("Robot rotation after positioning in front of backdrop x " + animationRobotGroup.getRotate());

            //## All of these coordinates and calculations can only be made after
            // the ParallelTranstion is complete, i.e. now.
            Rectangle cameraOnRobot = (Rectangle) animationRobotGroup.lookup("#" + animationRobotGroup.getId() + "_" + RobotFXCenterStageLG.CAMERA_ON_ROBOT_ID);
            Point2D cameraCoord = cameraOnRobot.localToScene(cameraOnRobot.getX(), cameraOnRobot.getY());

            // The returned coordinates of the objects are those of the upper left-hand
            // corner. We want to draw a line from the face of the camera to the center
            // of the AprilTag.
            double cameraFaceX = cameraCoord.getX() + cameraOnRobot.getWidth() / 2;
            double cameraFaceY = cameraCoord.getY();
            System.out.println("Camera face center x " + cameraFaceX + ", y " + cameraFaceY);

            Circle deviceOnRobot = (Circle) animationRobotGroup.lookup("#" + animationRobotGroup.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
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
            Line lineCTH = new Line(cameraFaceX, cameraFaceY, aprilTagCenterX, aprilTagCenterY);
            lineCTH.setId("lineCTH");
            lineCTH.setStroke(Color.LIME);
            lineCTH.getStrokeDashArray().addAll(10.0);
            lineCTH.setStrokeWidth(3.0);
            field.getChildren().add(lineCTH);

            // Draw the opposite side of the camera triangle.
            Line lineCTO = new Line(cameraFaceX, aprilTagCenterY, aprilTagCenterX, aprilTagCenterY);
            lineCTO.setId("lineCTO");
            lineCTO.setStroke(Color.LIME);
            lineCTO.getStrokeDashArray().addAll(10.0);
            lineCTO.setStrokeWidth(3.0);
            field.getChildren().add(lineCTO);

            // Draw the adjacent side of the camera triangle.
            Line lineCTA = new Line(cameraFaceX, cameraFaceY, cameraFaceX, aprilTagCenterY);
            lineCTA.setId("lineCTA");
            lineCTA.setStroke(Color.LIME);
            lineCTA.getStrokeDashArray().addAll(10.0);
            lineCTA.setStrokeWidth(3.0);
            field.getChildren().add(lineCTA);

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
            corrections = CameraToDeviceCorrections.getCameraToDeviceCorrections(degreesFromCameraToAprilTag,
                    distanceFromCameraToAprilTag,
                    animationRobot.cameraCenterFromRobotCenterPX + RobotFXCenterStageLG.CAMERA_HEIGHT / 2,
                    animationRobot.cameraOffsetFromRobotCenterPX,
                    animationRobot.deviceCenterFromRobotCenterPX, animationRobot.deviceOffsetFromRobotCenterPX);

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

        PauseTransition strafePauseT = new PauseTransition(Duration.millis(2500));
        strafePauseT.setOnFinished(event -> {
            removeCameraToTargetLines();

            // Draw two sides of the triangle formed between the center of
            // the robot and the target AprilTag.
            // Hypotenuse.
            //Line lineRCTH = new Line(robotCoordX, robotCoordY, aprilTagCenterX, aprilTagCenterY);
            //lineRCTH.setId("lineRCTH");
            //lineRCTH.setStroke(Color.AQUA);
            //lineRCTH.getStrokeDashArray().addAll(10.0);
            //lineRCTH.setStrokeWidth(3.0);
            //field.getChildren().add(lineRCTH);

            // Opposite.
            Line lineRCTO = new Line(robotCoordX, aprilTagCenterY, aprilTagCenterX, aprilTagCenterY);
            lineRCTO.setId("lineRCTO");
            lineRCTO.setStroke(Color.AQUA);
            lineRCTO.getStrokeDashArray().addAll(5.0);
            lineRCTO.setStrokeWidth(3.0);
            field.getChildren().add(lineRCTO);

            // Adjacent.
            Line lineRCTA = new Line(robotCoordX, robotCoordY, robotCoordX, aprilTagCenterY);
            lineRCTA.setId("lineRCTA");
            lineRCTA.setStroke(Color.AQUA);
            lineRCTA.getStrokeDashArray().addAll(5.0);
            lineRCTA.setStrokeWidth(3.0);
            field.getChildren().add(lineRCTA);

            // At the y-coordinate of the line that intersects the three AprilTags
            // draw a line that shows the distance from the device to the target
            // AprilTag.
            Line lineDTO = new Line(deviceCenterX, aprilTagCenterY, aprilTagCenterX, aprilTagCenterY);
            lineDTO.setId("lineDTO");
            lineDTO.setStroke(Color.YELLOW);
            lineDTO.getStrokeDashArray().addAll(10.0);
            lineDTO.setStrokeWidth(3.0);
            field.getChildren().add(lineDTO);

            // Draw a line between the device and the line that intersects the three
            // AprilTags.
            Line lineDTA = new Line(deviceCenterX, deviceCenterY, deviceCenterX, aprilTagCenterY);
            lineDTA.setId("lineDTA");
            lineDTA.setStroke(Color.YELLOW);
            lineDTA.getStrokeDashArray().addAll(10.0);
            lineDTA.setStrokeWidth(3.0);
            field.getChildren().add(lineDTA);
        });

        PauseTransition preStrafePauseT = new PauseTransition(Duration.millis(2500));
        preStrafePauseT.setOnFinished(event -> {
            removeRobotCenterToTargetLines();

            // Remove the pre-strafe device lines.
            Line lineDTORef = (Line) field.lookup("#lineDTO");
            Line lineDTARef = (Line) field.lookup("#lineDTA");
            field.getChildren().removeAll(lineDTORef, lineDTARef);
        });

        PauseTransition robotCenterToDevicePauseT = new PauseTransition(Duration.millis(2500));
        robotCenterToDevicePauseT.setOnFinished(event -> {
            removeCameraToTargetLines();

            // Draw the hypotenuse of the triangle formed between the center of the
            // robot and the delivery device.
            Line lineRCDH = new Line(robotCoordX, robotCoordY,
                    Math.abs(robotCoordX - animationRobot.deviceOffsetFromRobotCenterPX), aprilTagCenterY);
            lineRCDH.setId("lineRCDH");
            lineRCDH.setStroke(Color.DEEPPINK);
            lineRCDH.getStrokeDashArray().addAll(10.0);
            lineRCDH.setStrokeWidth(3.0);
            field.getChildren().add(lineRCDH);

            // Draw a line from the end point of the hypotenuse down to the center of
            // the device.
            Line lineDC = new Line(Math.abs(robotCoordX - animationRobot.deviceOffsetFromRobotCenterPX), aprilTagCenterY,
                    Math.abs(robotCoordX - animationRobot.deviceOffsetFromRobotCenterPX), Math.abs(robotCoordY - animationRobot.deviceCenterFromRobotCenterPX));
            lineDC.setId("lineDC");
            lineDC.setStroke(Color.DEEPPINK);
            lineDC.getStrokeDashArray().addAll(10.0, 7.0);
            lineDC.setStrokeWidth(3.0);
            field.getChildren().add(lineDC);

            // Draw the opposite side of the triangle, which is the distance from the
            // center of the robot to the device. The y-coordinate of this line is at
            // the level of the AprilTag.
            Line lineRCDO = new Line(robotCoordX, aprilTagCenterY,
                    Math.abs(robotCoordX - animationRobot.deviceOffsetFromRobotCenterPX), aprilTagCenterY);
            lineRCDO.setId("lineRCDO");
            lineRCDO.setStroke(Color.DEEPPINK);
            lineRCDO.getStrokeDashArray().addAll(10.0, 7.0);
            lineRCDO.setStrokeWidth(3.0);
            field.getChildren().add(lineRCDO);

            // Draw the adjacent side of the triangle.
            Line lineRCDA = new Line(robotCoordX, robotCoordY, robotCoordX, aprilTagCenterY);
            lineRCDA.setId("lineRCDA");
            lineRCDA.setStroke(Color.DEEPPINK);
            lineRCDA.getStrokeDashArray().addAll(10.0, 7.0);
            lineRCDA.setStrokeWidth(3.0);
            field.getChildren().add(lineRCDA);

            // Draw the triangle formed between the center of the robot and the target
            // AprilTag.
            // Hypotenuse.
            Line lineRCTH = new Line(robotCoordX, robotCoordY, aprilTagCenterX, aprilTagCenterY);
            lineRCTH.setId("lineRCTH");
            lineRCTH.setStroke(Color.AQUA);
            lineRCTH.getStrokeDashArray().addAll(7.0, 10.0);
            lineRCTH.setStrokeDashOffset(7.0);
            lineRCTH.setStrokeWidth(3.0);
            field.getChildren().add(lineRCTH);

            // Opposite.
            // Line lineRCTO = new Line(robotCoordX, aprilTagCenterY, aprilTagCenterX, aprilTagCenterY);
            // lineRCTO.setId("lineRCTO");
            // lineRCTO.setStroke(Color.AQUA);
            // lineRCTO.getStrokeDashArray().addAll(7.0, 10.0);
            // lineRCTO.setStrokeDashOffset(7.0);
            // lineRCTO.setStrokeWidth(3.0);
            // field.getChildren().add(lineRCTO);

            // The adjacent side of this triangle is shared with the robot center
            // to device triangle.
            Line lineRCTA = new Line(robotCoordX, robotCoordY, robotCoordX, aprilTagCenterY);
            lineRCTA.setId("lineRCTA");
            lineRCTA.setStroke(Color.AQUA);
            lineRCTA.getStrokeDashArray().addAll(7.0, 10.0);
            lineRCTA.setStrokeDashOffset(7.0);
            lineRCTA.setStrokeWidth(3.0);
            field.getChildren().add(lineRCTA);
        });

        PauseTransition preRotationPauseT = new PauseTransition(Duration.millis(2500));
        preRotationPauseT.setOnFinished(event -> {
            // Erase the lines from robot center to device.
            Line lineRCDHRef = (Line) field.lookup("#lineRCDH");
            Line lineDCRef = (Line) field.lookup("#lineDC");
            Line lineRCDORef = (Line) field.lookup("#lineRCDO");
            Line lineRCDARef = (Line) field.lookup("#lineRCDA");
            field.getChildren().removeAll(lineRCDHRef, lineDCRef, lineRCDORef, lineRCDARef);

            // Erase lines from robot center to target AprilTag.
            Line lineRCTHRef = (Line) field.lookup("#lineRCTH");
            Line lineRCTORef = (Line) field.lookup("#lineRCTO");
            Line lineRCTARef = (Line) field.lookup("#lineRCTA");
            field.getChildren().removeAll(lineRCTHRef, lineRCTORef, lineRCTARef);
        });

        // Look at the startup parameter that indicates whether to strafe or rotate.
        SequentialTransition seqTransition = new SequentialTransition(postPreviewPauseT, parallelT); // common
        switch (radioButtonText) {
            case "Strafe robot" -> seqTransition.getChildren().addAll(strafePauseT, preStrafePauseT, strafeTT);
            case "Turn robot" ->
                    seqTransition.getChildren().addAll(robotCenterToDevicePauseT, preRotationPauseT, rotateDeviceTowardsAprilTagT);
            case "Turn turret" -> seqTransition.getChildren().add(turretToTargetPauseT);
            default -> throw new AutonomousRobotException(TAG, "Unrecognized radio button text " + radioButtonText);
        }

        new PlayPauseToggle(pPlayPauseButton, seqTransition, cubicCurveTo);
    }

    private void removeCameraToTargetLines() {
        Line lineCTHRef = (Line) field.lookup("#lineCTH");
        Line lineCTORef = (Line) field.lookup("#lineCTO");
        Line lineCTARef = (Line) field.lookup("#lineCTA");
        field.getChildren().removeAll(lineCTHRef, lineCTORef, lineCTARef);
    }

    private void removeRobotCenterToTargetLines() {
        // Erase lines from robot center to target AprilTag.
        Line lineRCTHRef = (Line) field.lookup("#lineRCTH");
        Line lineRCTORef = (Line) field.lookup("#lineRCTO");
        Line lineRCTARef = (Line) field.lookup("#lineRCTA");
        field.getChildren().removeAll(lineRCTHRef, lineRCTORef, lineRCTARef);
    }

    private class PlayPauseToggle {

        private enum PlayPauseButtonStateOnPress {FIRST_PLAY, RESUME_PLAY, PAUSE}

        private PlayPauseButtonStateOnPress playPauseButtonStateOnPress;

        private final Button playPauseButton;
        private final SequentialTransition sequentialTransition;

        // Assume when this class is constructed that the Play button has already been
        // pressed.
        private PlayPauseToggle(Button pPlayPauseButton, SequentialTransition pSequentialTransaction,
                                CubicCurveTo pCubicCurveTo) {
            playPauseButton = pPlayPauseButton;
            sequentialTransition = pSequentialTransaction;
            playPauseButtonStateOnPress = PlayPauseButtonStateOnPress.PAUSE; // state for the next button press

            // When the SequentialTransitions are complete, disable the play/pause button.
            sequentialTransition.statusProperty().addListener((observableValue, oldValue, newValue) -> {
                        if (newValue == Animation.Status.STOPPED)
                            playPauseButton.setDisable(true);
                    }
            );

            // Action event for the play/pause button.
            EventHandler<ActionEvent> event = e -> {
                switch (playPauseButtonStateOnPress) {
                    case FIRST_PLAY -> {
                        // We're done with the Preview so we can use the final position of the
                        // preview robot as the target position for the animation robot.

                        //## The curves are a proof-of-concept. They will be different depending
                        // on the user's selection for the final position in front of the backdrop.
                        // CubicCurveTo constructor parameters: controlX1, controlX2, controlY1, controlY2, endX, endY

                        // Instead of getting the coordinates, which are those of the center
                        // of the robot, from the start parameters get them from the preview
                        // robot - because its position may have changed by drag-and-release.
                        Group previewRobotGroup = previewRobot.getRobot();
                        Bounds previewRobotBounds = previewRobotGroup.getBoundsInParent();
                        double robotPositionAtBackdropX = previewRobotBounds.getCenterX();
                        double robotPositionAtBackdropY = previewRobotBounds.getCenterY();

                        // The position of the animation robot in front of the backdrop can
                        // only be logged now that the preview robot drag-release is complete.
                        System.out.println("Animation robot approach position at the backdrop " + robotPositionAtBackdropX + ", y " + robotPositionAtBackdropY);

                        pCubicCurveTo.setX(robotPositionAtBackdropX);
                        pCubicCurveTo.setY(robotPositionAtBackdropY);
                        logFTCFieldCoordinates(alliance, robotPositionAtBackdropX, robotPositionAtBackdropY);

                        // Clear the preview robot and the camera field-of-view lines.
                        Line fovLeft = (Line) field.lookup("#" + PreviewDragAndRelease.CAMERA_FOV_LINE_LEFT);
                        Line fovRight = (Line) field.lookup("#" + PreviewDragAndRelease.CAMERA_FOV_LINE_RIGHT);
                        field.getChildren().removeAll(previewRobotGroup, fovLeft, fovRight);

                        // Now show the animation robot.
                        field.getChildren().add(animationRobotGroup);

                        playPauseButton.setText("Pause");
                        playPauseButtonStateOnPress = PlayPauseButtonStateOnPress.PAUSE;
                        sequentialTransition.play();
                    }
                    case RESUME_PLAY -> {
                        if (sequentialTransition.getStatus() != Animation.Status.STOPPED) {
                            playPauseButton.setText("Pause");
                            playPauseButtonStateOnPress = PlayPauseButtonStateOnPress.PAUSE;
                            sequentialTransition.play();
                        }
                    }
                    case PAUSE -> {
                        if (sequentialTransition.getStatus() != Animation.Status.STOPPED) {
                            sequentialTransition.pause();
                            playPauseButton.setText("Play");
                            playPauseButtonStateOnPress = PlayPauseButtonStateOnPress.RESUME_PLAY;
                        }
                    }
                    default ->
                            throw new AutonomousRobotException(TAG, "Invalid button state " + playPauseButtonStateOnPress);
                }
            };

            playPauseButton.setOnAction(event);
            playPauseButton.setText("Play");
            playPauseButtonStateOnPress = PlayPauseButtonStateOnPress.FIRST_PLAY;
        }
    }

    // The simulation show half of the FTC field. Based on the conventions for the
    // FTC field coordinates, FTC 0,0 is at the lower right of the blue half of the
    // field and FTC 0,0 is at the lower left of the red half of the field.
    private void logFTCFieldCoordinates(RobotConstants.Alliance pAlliance, double pRobotX, double pRobotY) {
        if (pAlliance == RobotConstants.Alliance.BLUE) {
            double ftcRobotXIn = FieldFXCenterStageBackdropLG.HALF_FIELD_DIMENSIONS_IN - (pRobotY / FieldFXCenterStageBackdropLG.PX_PER_INCH);
            double ftcRobotYIN = FieldFXCenterStageBackdropLG.HALF_FIELD_DIMENSIONS_IN - (pRobotX / FieldFXCenterStageBackdropLG.PX_PER_INCH);
            System.out.println("BLUE alliance FTC field coordinates: robot center x " + ftcRobotXIn + ", y " + ftcRobotYIN);
        } else { // must be RED
            double ftcRobotXIn = FieldFXCenterStageBackdropLG.HALF_FIELD_DIMENSIONS_IN - (pRobotY / FieldFXCenterStageBackdropLG.PX_PER_INCH);
            double ftcRobotYIN = 0 - (pRobotX / FieldFXCenterStageBackdropLG.PX_PER_INCH);
            System.out.println("RED alliance FTC field coordinates: robot center x " + ftcRobotXIn + ", y " + ftcRobotYIN);
        }
    }

}
