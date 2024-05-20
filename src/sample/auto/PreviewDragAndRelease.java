package sample.auto;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import sample.auto.fx.CenterStageControllerLG;
import sample.auto.fx.FieldFXCenterStageBackdropLG;
import sample.auto.fx.RobotFXCenterStageLG;
import sample.auto.fx.RobotFXLG;

import java.util.Locale;

public class PreviewDragAndRelease {
    public static final String CAMERA_FOV_LINE_LEFT_ID = "fovLineLeft";
    public static final String CAMERA_FOV_LINE_RIGHT_ID = "fovLineRight";

    private final CenterStageControllerLG controller;
    private final Pane field;
    private final RobotFXCenterStageLG previewRobot;
    private final Rectangle cameraOnRobot;
    private Line fovLineLeft;
    private Line fovLineRight;
    private double orgRobotMouseX, orgRobotMouseY;
    private double orgRobotTranslateX, orgRobotTranslateY;
    private Bounds robotBodyBounds;
    private double orgCameraMouseX, orgCameraMouseY;
    private double orgCameraTranslateX, orgCameraTranslateY;
    private Bounds cameraBounds;
    private double orgDeviceMouseX, orgDeviceMouseY;
    private double orgDeviceTranslateX, orgDeviceTranslateY;
    private Bounds deviceBounds;

    //!! For background see the answer from jewelsea in:
    // https://stackoverflow.com/questions/34887546/javafx-check-if-the-mouse-is-on-nodes-children
    // This post contains a link to:
    // https://docs.oracle.com/javase/8/javafx/events-tutorial/processing.htm
    //!! which talks about Event Filters (event capturing phase) and Event Handlers
    // (event bubbling phase). If we use EventHandlers then the children of the
    // Group will be processed first in the event bubbling phase. Then we can
    // consume the event and prevent it from bubbling upwards.

    public PreviewDragAndRelease(CenterStageControllerLG pController, Pane pField,
                                 Rectangle pApproachZone, RobotFXCenterStageLG pPreviewRobot,
                                 Rectangle pTargetAprilTag, Button pPlayButton, Button pSaveButton) {

        // Show the preview robot on the field.
        controller = pController;
        field = pField;
        previewRobot = pPreviewRobot;

        Group previewRobotGroup = pPreviewRobot.getRobot();
        pField.getChildren().add(previewRobotGroup);

        cameraOnRobot = (Rectangle) previewRobotGroup.lookup("#" + previewRobotGroup.getId() + "_" + RobotFXCenterStageLG.CAMERA_ON_ROBOT_ID);

        // Get the y coordinate of the target AprilTag.
        Point2D aprilTagCoord = pTargetAprilTag.localToScene(pTargetAprilTag.getX(), pTargetAprilTag.getY());
        double aprilTagCenterX = aprilTagCoord.getX() + pTargetAprilTag.getWidth() / 2;
        double aprilTagCenterY = aprilTagCoord.getY() + pTargetAprilTag.getHeight() / 2;

        // Draw the camera FOV lines from the default position of the camera.
        // If the target is outside of the camera's FOV, disable the Play and Save buttons.
        if (drawCameraFOV(cameraOnRobot, pPreviewRobot.cameraFieldOfView, aprilTagCenterX, aprilTagCenterY)) {
            pPlayButton.setDisable(false);
            pSaveButton.setDisable(false);
        } else {
            pPlayButton.setDisable(true);
            pSaveButton.setDisable(true);
        }

        // Get the robot body, which defines the limits of the camera and device.
        Rectangle robotBody = (Rectangle) previewRobotGroup.lookup("#" + previewRobotGroup.getId() + "_" + RobotFXLG.ROBOT_BODY_ID);
        cameraOnRobot.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
            orgCameraMouseX = mouseEvent.getSceneX();
            orgCameraMouseY = mouseEvent.getSceneY();
            orgCameraTranslateX = cameraOnRobot.getTranslateX();
            orgCameraTranslateY = cameraOnRobot.getTranslateY();
            cameraBounds = cameraOnRobot.getLayoutBounds();
        });

        Circle deviceOnRobot = (Circle) previewRobotGroup.lookup("#" + previewRobotGroup.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
        deviceOnRobot.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
            orgDeviceMouseX = mouseEvent.getSceneX();
            orgDeviceMouseY = mouseEvent.getSceneY();
            orgDeviceTranslateX = deviceOnRobot.getTranslateX();
            orgDeviceTranslateY = deviceOnRobot.getTranslateY();
            deviceBounds = deviceOnRobot.getLayoutBounds();
        });

        // --- remember initial coordinates of mouse cursor and nodes
        previewRobotGroup.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
            orgRobotMouseX = mouseEvent.getSceneX();
            orgRobotMouseY = mouseEvent.getSceneY();
            orgRobotTranslateX = previewRobotGroup.getTranslateX();
            orgRobotTranslateY = previewRobotGroup.getTranslateY();
            robotBodyBounds = robotBody.getLayoutBounds();
        });

        cameraOnRobot.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
            mouseEvent.consume();

            double offsetX = mouseEvent.getSceneX() - orgCameraMouseX;
            double offsetY = mouseEvent.getSceneY() - orgCameraMouseY;

            // Drag the camera.
            double currentTranslateX = cameraOnRobot.getTranslateX();
            double currentTranslateY = cameraOnRobot.getTranslateY();
            double newCameraTranslateX = orgCameraTranslateX + offsetX;
            double newCameraTranslateY = orgCameraTranslateY + offsetY;
            cameraOnRobot.setTranslateX(newCameraTranslateX);
            cameraOnRobot.setTranslateY(newCameraTranslateY);

            // Make sure the new position of the camera is within the bounds
            // of the robot body.
            if (cameraBounds.getMinX() + newCameraTranslateX < robotBodyBounds.getMinX() ||
                    cameraBounds.getMaxX() + newCameraTranslateX > robotBodyBounds.getMaxX() ||
                    cameraBounds.getMinY() + newCameraTranslateY < robotBodyBounds.getMinY() ||
                    cameraBounds.getMaxY() + newCameraTranslateY > robotBodyBounds.getMaxY()) {

                // Revert to the last good position.
                cameraOnRobot.setTranslateX(currentTranslateX);
                cameraOnRobot.setTranslateY(currentTranslateY);
                return;
            }

            // Remove the current FOV lines and redraw them from the new
            // camera position. If the target is outside of the camera's FOV,
            // disable the Play and Save buttons.
            if (drawCameraFOV(cameraOnRobot, pPreviewRobot.cameraFieldOfView, aprilTagCenterX, aprilTagCenterY)) {
                pPlayButton.setDisable(false);
                pSaveButton.setDisable(false);
            } else {
                pPlayButton.setDisable(true);
                pSaveButton.setDisable(true);
            }

            // Get the distance in pixels between robot center and camera center,
            // both fore and aft and side to side, set the correct sign for FTC,
            // and convert to inches.
            Bounds cameraBoundsInScene = cameraOnRobot.localToScene(cameraOnRobot.getBoundsInLocal());
            Bounds robotBodyBoundsInScene = robotBody.localToScene(robotBody.getBoundsInLocal());

            double newCameraCenterFromRobotCenter = cameraBoundsInScene.getCenterY() - robotBodyBoundsInScene.getCenterY();
            newCameraCenterFromRobotCenter *= -1; // FTC direction
            newCameraCenterFromRobotCenter /= FieldFXCenterStageBackdropLG.PX_PER_INCH;

            double newCameraOffsetFromRobotCenter = cameraBoundsInScene.getCenterX() - robotBodyBoundsInScene.getCenterX();
            newCameraOffsetFromRobotCenter *= -1; // FTC direction
            newCameraOffsetFromRobotCenter /= FieldFXCenterStageBackdropLG.PX_PER_INCH;

            // Update the start parameters display.
            pController.camera_center_from_robot_center.setText(String.format(Locale.US, "%.2f", newCameraCenterFromRobotCenter));
            pController.camera_offset_from_robot_center.setText(String.format(Locale.US, "%.2f", newCameraOffsetFromRobotCenter));
        });

        deviceOnRobot.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
            mouseEvent.consume();

            double offsetX = mouseEvent.getSceneX() - orgDeviceMouseX;
            double offsetY = mouseEvent.getSceneY() - orgDeviceMouseY;

            // Drag the device.
            double currentTranslateX = deviceOnRobot.getTranslateX();
            double currentTranslateY = deviceOnRobot.getTranslateY();
            double newDeviceTranslateX = orgDeviceTranslateX + offsetX;
            double newDeviceTranslateY = orgDeviceTranslateY + offsetY;
            deviceOnRobot.setTranslateX(newDeviceTranslateX);
            deviceOnRobot.setTranslateY(newDeviceTranslateY);

            // Make sure the new position of the device is within the bounds
            // of the robot body.
            if (deviceBounds.getMinX() + newDeviceTranslateX < robotBodyBounds.getMinX() ||
                    deviceBounds.getMaxX() + newDeviceTranslateX > robotBodyBounds.getMaxX() ||
                    deviceBounds.getMinY() + newDeviceTranslateY < robotBodyBounds.getMinY() ||
                    deviceBounds.getMaxY() + newDeviceTranslateY > robotBodyBounds.getMaxY()) {

                // Revert to the last good position.
                deviceOnRobot.setTranslateX(currentTranslateX);
                deviceOnRobot.setTranslateY(currentTranslateY);
                return;
            }

            // Get the distance in pixels between robot center and device center,
            // both fore and aft and side to side, set the correct sign for FTC,
            // and convert to inches.
            Bounds deviceBoundsInScene = deviceOnRobot.localToScene(deviceOnRobot.getBoundsInLocal());
            Bounds robotBodyBoundsInScene = robotBody.localToScene(robotBody.getBoundsInLocal());

            double newDeviceCenterFromRobotCenter = deviceBoundsInScene.getCenterY() - robotBodyBoundsInScene.getCenterY();
            newDeviceCenterFromRobotCenter *= -1; // FTC direction
            newDeviceCenterFromRobotCenter /= FieldFXCenterStageBackdropLG.PX_PER_INCH;

            double newDeviceOffsetFromRobotCenter = deviceBoundsInScene.getCenterX() - robotBodyBoundsInScene.getCenterX();
            newDeviceOffsetFromRobotCenter *= -1; // FTC direction
            newDeviceOffsetFromRobotCenter /= FieldFXCenterStageBackdropLG.PX_PER_INCH;

            // Update the start parameters display.
            pController.device_center_from_robot_center.setText(String.format(Locale.US, "%.2f", newDeviceCenterFromRobotCenter));
            pController.device_offset_from_robot_center.setText(String.format(Locale.US, "%.2f", newDeviceOffsetFromRobotCenter));
        });

        // --- Coordinated drag of nodes calculated from mouse cursor movement
        previewRobotGroup.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
            double offsetX = mouseEvent.getSceneX() - orgRobotMouseX;
            double offsetY = mouseEvent.getSceneY() - orgRobotMouseY;
            double currentTranslateX = previewRobotGroup.getTranslateX();
            double currentTranslateY = previewRobotGroup.getTranslateY();

            // Drag the robot.
            double newRobotTranslateX = orgRobotTranslateX + offsetX;
            double newRobotTranslateY = orgRobotTranslateY + offsetY;
            previewRobotGroup.setTranslateX(newRobotTranslateX);
            previewRobotGroup.setTranslateY(newRobotTranslateY);

            // Make sure the new position of the preview robot is within the bounds
            // of the approach zone.
            Bounds previewRobotBounds = previewRobotGroup.getBoundsInParent();
            Bounds approachZoneBounds = pApproachZone.getBoundsInParent();
            if (previewRobotBounds.getMinX() < approachZoneBounds.getMinX() ||
                    previewRobotBounds.getMaxX() > approachZoneBounds.getMaxX() ||
                    previewRobotBounds.getMinY() < approachZoneBounds.getMinY() ||
                    previewRobotBounds.getMaxY() > approachZoneBounds.getMaxY()) {

                // Revert to the last good position.
                previewRobotGroup.setTranslateX(currentTranslateX);
                previewRobotGroup.setTranslateY(currentTranslateY);
                return;
            }

            // Remove the current FOV lines and redraw them from the new
            // camera position. If the target is outside of the camera's FOV,
            // disable the Play and Save buttons.
            if (drawCameraFOV(cameraOnRobot, pPreviewRobot.cameraFieldOfView, aprilTagCenterX, aprilTagCenterY)) {
                pPlayButton.setDisable(false);
                pSaveButton.setDisable(false);
            } else {
                pPlayButton.setDisable(true);
                pSaveButton.setDisable(true);
            }

            // Update the start parameter display with the new x and y
            // positions of the center of the preview robot.
            double previewRobotCenterInX = previewRobotBounds.getCenterX() / FieldFXCenterStageBackdropLG.PX_PER_INCH;
            double previewRobotCenterInY = previewRobotBounds.getCenterY() / FieldFXCenterStageBackdropLG.PX_PER_INCH;

            pController.robot_position_at_backdrop_x.setText(String.format(Locale.US, "%.2f", previewRobotCenterInX));
            pController.robot_position_at_backdrop_y.setText(String.format(Locale.US, "%.2f", previewRobotCenterInY));
     });
    }

    private boolean drawCameraFOV(Rectangle pCamera, double pCameraFOV,
                               double pAprilTagCenterX, double pAprilTagCenterY) {
        boolean targetWithinFOV;

        // Remove the current FOV lines.
        field.getChildren().removeAll(fovLineLeft, fovLineRight);

        Point2D cameraCoord = pCamera.localToScene(pCamera.getX(), pCamera.getY());
        double cameraFaceX = cameraCoord.getX() + pCamera.getWidth() / 2;
        double cameraFaceY = cameraCoord.getY();

        // Get the y coordinate of the target.
        double adjustedAprilTagCenterY = pAprilTagCenterY - 10.0; // a point a little higher than the target

        // Get the adjacent side of the triangle from the camera to the target AprilTag.
        double fovAdjacent = Math.abs(cameraFaceY - adjustedAprilTagCenterY);
        // tangent  = opposite / adjacent
        double halfFOVTan = Math.tan(Math.toRadians(pCameraFOV / 2));
        double halfFOVOpposite = halfFOVTan * fovAdjacent;

        fovLineLeft = new Line(cameraFaceX, cameraFaceY, cameraFaceX - halfFOVOpposite, adjustedAprilTagCenterY);
        fovLineLeft.setId(CAMERA_FOV_LINE_LEFT_ID);
        fovLineLeft.setStroke(Color.CORAL);
        fovLineLeft.getStrokeDashArray().addAll(10.0);
        fovLineLeft.setStrokeWidth(3.0);
        field.getChildren().add(fovLineLeft);

        fovLineRight = new Line(cameraFaceX, cameraFaceY, cameraFaceX + halfFOVOpposite, adjustedAprilTagCenterY);
        fovLineRight.setId(CAMERA_FOV_LINE_RIGHT_ID);
        fovLineRight.setStroke(Color.CORAL);
        fovLineRight.getStrokeDashArray().addAll(10.0);
        fovLineRight.setStrokeWidth(3.0);
        field.getChildren().add(fovLineRight);

        // If the target AprilTag is outside of the camera's field of view,
        // turn the display of FOV degrees in the start parameters area to red.
        Point2D updatedCameraCoord = cameraOnRobot.localToScene(cameraOnRobot.getX(), cameraOnRobot.getY());
        double updatedCameraFaceX = updatedCameraCoord.getX() + cameraOnRobot.getWidth() / 2;
        double updatedCameraFaceY = updatedCameraCoord.getY();

        // Get the adjacent side of the triangle from the camera to the AprilTag.
        double updatedFOVAdjacent = Math.abs(updatedCameraFaceY - pAprilTagCenterY);
        // tangent  = opposite / adjacent
        double updatedHalfFOVTan = Math.tan(Math.toRadians(previewRobot.cameraFieldOfView / 2));
        double updatedHalfFOVOpposite = updatedHalfFOVTan * updatedFOVAdjacent;

        double leftFOVBoundaryAtAprilTag = updatedCameraFaceX - updatedHalfFOVOpposite;
        double rightFOVBoundaryAtAprilTag = updatedCameraFaceX + updatedHalfFOVOpposite;

        if (pAprilTagCenterX < leftFOVBoundaryAtAprilTag || pAprilTagCenterX > rightFOVBoundaryAtAprilTag) {
            // From jewelsea's answer in
            // https://stackoverflow.com/questions/24702542/how-to-change-the-color-of-text-in-javafx-textfield
            // textField.setStyle("-fx-text-inner-color: red;");
            controller.camera_field_of_view.setStyle("-fx-text-inner-color: red; -fx-font-weight: bold;");
            targetWithinFOV = false;
        } else {
            controller.camera_field_of_view.setStyle("-fx-text-inner-color: black; -fx-font-weight: normal;");
            targetWithinFOV = true;
        }

        return targetWithinFOV;
    }
}

