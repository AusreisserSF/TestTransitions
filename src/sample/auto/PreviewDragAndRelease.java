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

    private double orgSceneX, orgSceneY;
    private double orgRobotTranslateX, orgRobotTranslateY;
    private double previousRobotTranslateX, previousRobotTranslateY;
    private double orgFOVLineLeftTranslateX, orgFOVLineLeftTranslateY;
    private double orgFOVLineRightTranslateX, orgFOVLineRightTranslateY;

    //**TODO During Preview turn the uneditable FOV red in the
    // start parameters if the user drags the preview robot to the point where
    // the AprilTag target is outside the FOV. FieldFXCenterStageBackdropLG
    // contains constants for the center points of each AprilTag.

    public PreviewDragAndRelease(CenterStageControllerLG pController, Pane pField, Rectangle pApproachZone, Group pPreviewRobot) {

        // Show the preview robot on the field.
        pField.getChildren().add(pPreviewRobot);

        Rectangle cameraOnRobot = (Rectangle) pPreviewRobot.lookup("#" + pPreviewRobot.getId() + "_" + RobotFXCenterStageLG.CAMERA_ON_ROBOT_ID);
        Point2D cameraCoord = cameraOnRobot.localToScene(cameraOnRobot.getX(), cameraOnRobot.getY());
        double cameraFaceX = cameraCoord.getX() + cameraOnRobot.getWidth() / 2;
        double cameraFaceY = cameraCoord.getY();

        // Get the y coordinates of any AprilTag - they're all the same.
        Rectangle aprilTag = (Rectangle) pField.lookup("#" + FieldFXCenterStageBackdropLG.APRIL_TAG_1_ID);
        Point2D aprilTagCoord = aprilTag.localToScene(aprilTag.getX(), aprilTag.getY());
        double aprilTagCenterY = aprilTagCoord.getY() + aprilTag.getHeight() / 2;

        // Get the adjacent side of the triangle from the camera to the AprilTag.
        double fovAdjacent = Math.abs(cameraFaceY - aprilTagCenterY);
        // tangent  = opposite / adjacent
        //**TODO Don't get the FOV from the controller - put it into the robot Group.
        double halfFOVTan = Math.tan(Math.toRadians(Double.parseDouble(pController.camera_field_of_view.getText()) / 2));
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
        pPreviewRobot.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
            orgSceneX = mouseEvent.getSceneX();
            orgSceneY = mouseEvent.getSceneY();

            orgRobotTranslateX = pPreviewRobot.getTranslateX();
            orgRobotTranslateY = pPreviewRobot.getTranslateY();
            previousRobotTranslateX = orgRobotTranslateX;
            previousRobotTranslateY = orgRobotTranslateY;
            orgFOVLineLeftTranslateX = fovLineLeft.getTranslateX();
            orgFOVLineLeftTranslateY = fovLineLeft.getTranslateY();
            orgFOVLineRightTranslateX = fovLineRight.getTranslateX();
            orgFOVLineRightTranslateY = fovLineRight.getTranslateY();
        });

        // --- Coordinated drag of nodes calculated from mouse cursor movement
        pPreviewRobot.addEventFilter(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
            double offsetX = mouseEvent.getSceneX() - orgSceneX;
            double offsetY = mouseEvent.getSceneY() - orgSceneY;

            // Drag the robot.
            double newRobotTranslateX = orgRobotTranslateX + offsetX;
            double newRobotTranslateY = orgRobotTranslateY + offsetY;
            pPreviewRobot.setTranslateX(newRobotTranslateX);
            pPreviewRobot.setTranslateY(newRobotTranslateY);

            // Make sure the new position of the preview robot is withing the bounds
            // of the approach zone.
            Bounds previewRobotBounds = pPreviewRobot.getBoundsInParent();
            Bounds approachZoneBounds = pApproachZone.getBoundsInParent();
            if (previewRobotBounds.getMinX() < approachZoneBounds.getMinX() ||
                    previewRobotBounds.getMaxX() > approachZoneBounds.getMaxX() ||
                    previewRobotBounds.getMinY() < approachZoneBounds.getMinY() ||
                    previewRobotBounds.getMaxY() > approachZoneBounds.getMaxY()) {

                // Revert to the last good position.
                pPreviewRobot.setTranslateX(previousRobotTranslateX);
                pPreviewRobot.setTranslateY(previousRobotTranslateY);
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
        });
    }
}

