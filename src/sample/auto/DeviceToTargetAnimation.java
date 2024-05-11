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

            // Draw a line from the device to the AprilTag.
            Circle deviceOnRobot = (Circle) animationRobotGroup.lookup("#" + animationRobotGroup.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
            Point2D deviceCoord = deviceOnRobot.localToScene(deviceOnRobot.getCenterX(), deviceOnRobot.getCenterY());
            Line lineDAT = new Line(deviceCoord.getX(), deviceCoord.getY(), aprilTagCenterX, aprilTagCenterY);
            lineDAT.setId("lineDAT");
            lineDAT.setStroke(Color.YELLOW);
            lineDAT.getStrokeDashArray().addAll(10.0);
            lineDAT.setStrokeWidth(3.0);
            field.getChildren().add(lineDAT);
            System.out.println("Distance from device to AprilTag " + (aprilTagCenterY - deviceCoord.getY()));

            Bounds robotBP = animationRobotGroup.getBoundsInParent();
            System.out.println("Robot position after strafe x " + robotBP.getCenterX() + ", y " + robotBP.getCenterY());
            //**TODO Show or at least log post-strafe position in FTC field coordinates.
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

            // Draw a line from the device to the AprilTag.
            Circle deviceOnRobot = (Circle) animationRobotGroup.lookup("#" + animationRobotGroup.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
            Point2D deviceCoord = deviceOnRobot.localToScene(deviceOnRobot.getCenterX(), deviceOnRobot.getCenterY());
            Line lineDH = new Line(deviceCoord.getX(), deviceCoord.getY(), aprilTagCenterX, aprilTagCenterY);
            lineDH.setId("lineDCH");
            lineDH.setStroke(Color.YELLOW);
            lineDH.getStrokeDashArray().addAll(10.0);
            lineDH.setStrokeWidth(3.0);
            field.getChildren().add(lineDH);
        });

        //**TODO If we're treating the device as a turret then the robot itself
        // does not need to turn. Just draw the lines from the turret to the
        // target.
        PauseTransition turretToTargetPauseT = new PauseTransition(Duration.millis(750));
        turretToTargetPauseT.setOnFinished(event -> removeCameraToTargetLines());

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
            //**TODO Show or at least log position in FTC field coordinates?
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

        PauseTransition cameraToTargetPauseT = new PauseTransition(Duration.millis(2500));
        cameraToTargetPauseT.setOnFinished(event -> removeCameraToTargetLines());

        PauseTransition robotCenterToDevicePauseT = new PauseTransition(Duration.millis(2500));
        robotCenterToDevicePauseT.setOnFinished(event -> {
            removeCameraToTargetLines();

            // Draw the triangle formed between the center of the robot and the delivery device.
            Line lineRCDH = new Line(robotCoordX, robotCoordY, Math.abs(robotCoordX - animationRobot.deviceOffsetFromRobotCenterPX), aprilTagCenterY);
            lineRCDH.setId("lineRCDH");
            lineRCDH.setStroke(Color.FUCHSIA);
            lineRCDH.getStrokeDashArray().addAll(10.0);
            lineRCDH.setStrokeWidth(3.0);
            field.getChildren().add(lineRCDH);

            // Draw a line from the end point of the hypotenuse down through the center of the
            // device to the bottom of the robot. In general the device can be anywhere along
            // this line.
            Line lineDC = new Line(Math.abs(robotCoordX - animationRobot.deviceOffsetFromRobotCenterPX), aprilTagCenterY, Math.abs(robotCoordX - animationRobot.deviceOffsetFromRobotCenterPX), robotCoordX + animationRobot.robotHeightPX / 2);
            lineDC.setId("lineDC");
            lineDC.setStroke(Color.FUCHSIA);
            lineDC.getStrokeDashArray().addAll(10.0);
            lineDC.setStrokeWidth(3.0);
            field.getChildren().add(lineDC);

            Line lineRCDO = new Line(robotCoordX, aprilTagCenterY, Math.abs(robotCoordX - animationRobot.deviceOffsetFromRobotCenterPX), aprilTagCenterY);
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
        SequentialTransition seqTransition = new SequentialTransition(postPreviewPauseT, parallelT); // common
        switch (radioButtonText) {
            case "Strafe robot" -> seqTransition.getChildren().addAll(cameraToTargetPauseT, strafeTT);
            case "Turn robot" -> seqTransition.getChildren().addAll(robotCenterToDevicePauseT, preRotationPauseT, rotateDeviceTowardsAprilTagT);
            case "Turn turret" -> seqTransition.getChildren().add(turretToTargetPauseT);
            default -> throw new AutonomousRobotException(TAG, "Unrecognized radio button text " + radioButtonText);
        }

        new PlayPauseToggle(pPlayPauseButton, seqTransition, cubicCurveTo);
    }

    private void removeCameraToTargetLines() {
        Line lineCHRef = (Line) field.lookup("#lineCH");
        Line lineCORef = (Line) field.lookup("#lineCO");
        Line lineCARef = (Line) field.lookup("#lineCA");
        field.getChildren().removeAll(lineCHRef, lineCORef, lineCARef);
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
                    default -> throw new AutonomousRobotException(TAG, "Invalid button state " + playPauseButtonStateOnPress);
                }
            };

            playPauseButton.setOnAction(event);
            playPauseButton.setText("Play");
            playPauseButtonStateOnPress = PlayPauseButtonStateOnPress.FIRST_PLAY;
        }
    }

}
