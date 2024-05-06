package sample.auto;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import sample.auto.fx.FieldFXCenterStageBackdropLG;
import sample.auto.fx.RobotFXCenterStageLG;

public class PreviewDragAndRelease {
    public static final String CAMERA_FOV_LINE_LEFT = "lineHalfFOVLeft";
    public static final String CAMERA_FOV_LINE_RIGHT = "lineHalfFOVRight";

    private double orgSceneX, orgSceneY;
    private double orgRobotTranslateX, orgRobotTranslateY;
    private double orgFOVLineLeftTranslateX, orgFOVLineLeftTranslateY;
    private double orgFOVLineRightTranslateX, orgFOVLineRightTranslateY;

    //**TODO Is there a way to use the camera's FOV to validate the preview
    // position. Yes, if you make room on the start parameters screen and set
    // a parameter for it. Or just hardcode 78 degrees and put up an alert
    // after you get the camera to target angle in DeviceToTargetAnimation.
    //**TODO Put in the XML and validate ??but do not include in grid of start parameters
    // or as read-only??

    //**TODO As an experiment draw a 78 degree camera field of view.

    public PreviewDragAndRelease(Pane pField, Rectangle pApproachZone, Group pPreviewRobot) {

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
        // tan 39 degrees  = opposite / adjacent
        double halfFOVTan = Math.tan(Math.toRadians(39.0));
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

            //**TODO These are different for each Shape ... generalize by
            // putting into an EnumMap??
            orgRobotTranslateX = pPreviewRobot.getTranslateX();
            orgRobotTranslateY = pPreviewRobot.getTranslateY();
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

                //**TODO When you drag any edge of the preview robot outside the
                // bounds of the approach zone, JavaFX cancels the drag and places
                // the robot in its position before the drag. So you have to revert
                // the FOV lines also.

                //**TODO Because of the reversion you may not need to back out the change.
                pPreviewRobot.setTranslateX(newRobotTranslateX - offsetX);
                pPreviewRobot.setTranslateY(newRobotTranslateY - offsetY);
                return;
            }

            //**TODO Try updating the start parameter display with the new x and
            // y positions of the center of the preview robot. **LATER** - disable
            // all other TextFields but only disable editing of the position fields.
            double previewRobotCenterInX = previewRobotBounds.getCenterX() / FieldFXCenterStageBackdropLG.PX_PER_INCH;
            double previewRobotCenterInY = previewRobotBounds.getCenterY() / FieldFXCenterStageBackdropLG.PX_PER_INCH;

            //**TODO Need a reference to the controller or to the fields in the controller
            // // or to the GridPane (for lookup). ?? Use ToolTip during drag?
            // pCenterStageControllerLG.robot_position_at_backdrop_x = previewRobotCenterInX;
            // pCenterStageControllerLG.robot_position_at_backdrop_y = previewRobotCenterInY;

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

