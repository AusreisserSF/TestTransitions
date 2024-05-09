package sample.auto;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import sample.auto.fx.CenterStageControllerLG;
import sample.auto.fx.FieldFXCenterStageBackdropLG;
import sample.auto.fx.RobotFXCenterStageLG;

import java.util.Locale;

public class PreviewDragAndRelease {
    public static final String CAMERA_FOV_LINE_LEFT = "lineHalfFOVLeft";
    public static final String CAMERA_FOV_LINE_RIGHT = "lineHalfFOVRight";

    private final Rectangle cameraOnRobot;
    private final double aprilTagCenterY;
    private double orgSceneX, orgSceneY;
    private double orgRobotTranslateX, orgRobotTranslateY;
    private double previousRobotTranslateX, previousRobotTranslateY;
    private double orgFOVLineLeftTranslateX, orgFOVLineLeftTranslateY;
    private double orgFOVLineRightTranslateX, orgFOVLineRightTranslateY;

    //**TODO During Preview turn the uneditable FOV red in the
    // start parameters if the user drags the preview robot to the point where
    // the AprilTag target is outside the FOV.
    //**TODO Not so easy because the triangle between the center of the camera
    // face, the line that intersects all three AprilTags, and the distance
    // from the camera face to the target AprilTag changes during the drag-
    // and-release of the preview robot.

    public PreviewDragAndRelease(CenterStageControllerLG pController, Pane pField,
                                 Rectangle pApproachZone, RobotFXCenterStageLG pPreviewRobot,
                                 double pTargetAprilTagX) {

        // Show the preview robot on the field.
        Group previewRobotGroup = pPreviewRobot.getRobot();
        pField.getChildren().add(previewRobotGroup);

        cameraOnRobot = (Rectangle) previewRobotGroup.lookup("#" + previewRobotGroup.getId() + "_" + RobotFXCenterStageLG.CAMERA_ON_ROBOT_ID);
        Point2D cameraCoord = cameraOnRobot.localToScene(cameraOnRobot.getX(), cameraOnRobot.getY());
        double cameraFaceX = cameraCoord.getX() + cameraOnRobot.getWidth() / 2;
        double cameraFaceY = cameraCoord.getY();

        // Get the y coordinates of any AprilTag - they're all the same.
        Rectangle aprilTag = (Rectangle) pField.lookup("#" + FieldFXCenterStageBackdropLG.APRIL_TAG_1_ID);
        Point2D aprilTagCoord = aprilTag.localToScene(aprilTag.getX(), aprilTag.getY());
        aprilTagCenterY = aprilTagCoord.getY() + aprilTag.getHeight() / 2;

        // Get the adjacent side of the triangle from the camera to the AprilTag.
        double fovAdjacent = Math.abs(cameraFaceY - aprilTagCenterY);
        // tangent  = opposite / adjacent
        double halfFOVTan = Math.tan(Math.toRadians(pPreviewRobot.cameraFieldOfView / 2));
        double halfFOVOpposite = halfFOVTan * fovAdjacent;

        Line fovLineLeft = new Line(cameraFaceX, cameraFaceY, cameraFaceX - halfFOVOpposite, aprilTagCenterY);
        fovLineLeft.setId(CAMERA_FOV_LINE_LEFT);
        fovLineLeft.setStroke(Color.AQUA);
        fovLineLeft.getStrokeDashArray().addAll(10.0);
        fovLineLeft.setStrokeWidth(3.0);
        pField.getChildren().add(fovLineLeft);

        Line fovLineRight = new Line(cameraFaceX, cameraFaceY, cameraFaceX + halfFOVOpposite, aprilTagCenterY);
        fovLineRight.setId(CAMERA_FOV_LINE_RIGHT);
        fovLineRight.setStroke(Color.AQUA);
        fovLineRight.getStrokeDashArray().addAll(10.0);
        fovLineRight.setStrokeWidth(3.0);
        pField.getChildren().add(fovLineRight);

        // --- remember initial coordinates of mouse cursor and nodes
        previewRobotGroup.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
            orgSceneX = mouseEvent.getSceneX();
            orgSceneY = mouseEvent.getSceneY();

            orgRobotTranslateX = previewRobotGroup.getTranslateX();
            orgRobotTranslateY = previewRobotGroup.getTranslateY();
            previousRobotTranslateX = orgRobotTranslateX;
            previousRobotTranslateY = orgRobotTranslateY;
            orgFOVLineLeftTranslateX = fovLineLeft.getTranslateX();
            orgFOVLineLeftTranslateY = fovLineLeft.getTranslateY();
            orgFOVLineRightTranslateX = fovLineRight.getTranslateX();
            orgFOVLineRightTranslateY = fovLineRight.getTranslateY();
        });

        // --- Coordinated drag of nodes calculated from mouse cursor movement
        previewRobotGroup.addEventFilter(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
            double offsetX = mouseEvent.getSceneX() - orgSceneX;
            double offsetY = mouseEvent.getSceneY() - orgSceneY;

            // Drag the robot.
            double newRobotTranslateX = orgRobotTranslateX + offsetX;
            double newRobotTranslateY = orgRobotTranslateY + offsetY;
            previewRobotGroup.setTranslateX(newRobotTranslateX);
            previewRobotGroup.setTranslateY(newRobotTranslateY);

            // Make sure the new position of the preview robot is withing the bounds
            // of the approach zone.
            Bounds previewRobotBounds = previewRobotGroup.getBoundsInParent();
            Bounds approachZoneBounds = pApproachZone.getBoundsInParent();
            if (previewRobotBounds.getMinX() < approachZoneBounds.getMinX() ||
                    previewRobotBounds.getMaxX() > approachZoneBounds.getMaxX() ||
                    previewRobotBounds.getMinY() < approachZoneBounds.getMinY() ||
                    previewRobotBounds.getMaxY() > approachZoneBounds.getMaxY()) {

                // Revert to the last good position.
                previewRobotGroup.setTranslateX(previousRobotTranslateX);
                previewRobotGroup.setTranslateY(previousRobotTranslateY);
                return;
            }

            // Save known good position for next time around.
            previousRobotTranslateX = newRobotTranslateX;
            previousRobotTranslateY = newRobotTranslateY;

            // Updating the start parameter display with the new x and y
            // positions of the center of the preview robot.
            double previewRobotCenterInX = previewRobotBounds.getCenterX() / FieldFXCenterStageBackdropLG.PX_PER_INCH;
            double previewRobotCenterInY = previewRobotBounds.getCenterY() / FieldFXCenterStageBackdropLG.PX_PER_INCH;

            pController.robot_position_at_backdrop_x.setText(String.format(Locale.US, "%.2f", previewRobotCenterInX));
            pController.robot_position_at_backdrop_y.setText(String.format(Locale.US, "%.2f", previewRobotCenterInY));

            // Drag the left boundary of the camera field of view.
            double newFOVLineLeftTranslateX = orgFOVLineLeftTranslateX + offsetX;
            double newFOVLineLeftTranslateY = orgFOVLineLeftTranslateY + offsetY;
            fovLineLeft.setTranslateX(newFOVLineLeftTranslateX);
            fovLineLeft.setTranslateY(newFOVLineLeftTranslateY);

            // Drag the right boundary of the camera field of view.
            double newFOVLineRightTranslateX = orgFOVLineRightTranslateX + offsetX;
            double newFOVLineRightTranslateY = orgFOVLineRightTranslateY + offsetY;
            fovLineRight.setTranslateX(newFOVLineRightTranslateX);
            fovLineRight.setTranslateY(newFOVLineRightTranslateY);

            //**TODO It would be ideal to redraw the FOV lines as the preview
            // robot is dragged withing the approach zone. Then it would be
            // easy to know if the AprilTag is within the camera's FOV or not.
            // Do this in two steps: 1. trigonometry, validation, and changing
            // the font color of the FOV to red and 2. redraw the lines - try
            // in the testbed first.

            //**TODO Boundary checking is closer but still off; either the FOV window
            // is too far to the left or the AprilTag center is too far to the right.
            // Try AT in parent.

            Point2D updatedCameraCoord = cameraOnRobot.localToScene(cameraOnRobot.getX(), cameraOnRobot.getY());
            double updatedCameraFaceX = updatedCameraCoord.getX() + cameraOnRobot.getWidth() / 2;
            double updatedCameraFaceY = updatedCameraCoord.getY();

            // Get the adjacent side of the triangle from the camera to the AprilTag.
            double updatedFOVAdjacent = Math.abs(updatedCameraFaceY - aprilTagCenterY);
            // tangent  = opposite / adjacent
            double updatedHalfFOVTan = Math.tan(Math.toRadians(pPreviewRobot.cameraFieldOfView / 2));
            double updatedHalfFOVOpposite = updatedHalfFOVTan * updatedFOVAdjacent;

            double leftFOVBoundaryAtAprilTag = updatedCameraFaceX - updatedHalfFOVOpposite;
            double rightFOVBoundaryAtAprilTag = updatedCameraFaceX + updatedHalfFOVOpposite;

            if (pTargetAprilTagX < leftFOVBoundaryAtAprilTag || pTargetAprilTagX > rightFOVBoundaryAtAprilTag) {
                // The target AprilTag is outside of the camera's field of view.
                // From jewelsea's answer in
                // https://stackoverflow.com/questions/24702542/how-to-change-the-color-of-text-in-javafx-textfield
                // textField.setStyle("-fx-text-inner-color: red;");
                pController.camera_field_of_view.setStyle("-fx-text-inner-color: red; -fx-font-weight: bold;");
            } else
                pController.camera_field_of_view.setStyle("-fx-text-inner-color: black; -fx-font-weight: normal;");
        });
    }
}

