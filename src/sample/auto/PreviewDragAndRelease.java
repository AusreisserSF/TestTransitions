package sample.auto;

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

    public PreviewDragAndRelease(Pane pField, Group pPreviewRobot) {

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

        //**TODO Disallow movement if the preview robot crosses the approach
        // zone boundary.
        // See use of "intersects" in
        // https://stackoverflow.com/questions/15013913/checking-collision-of-shapes-with-javafx
        // --- Coordinated drag of nodes calculated from mouse cursor movement
        pPreviewRobot.addEventFilter(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
            double offsetX = mouseEvent.getSceneX() - orgSceneX;
            double offsetY = mouseEvent.getSceneY() - orgSceneY;

            // Drag the robot.
            double newRobotTranslateX = orgRobotTranslateX + offsetX;
            double newRobotTranslateY = orgRobotTranslateY + offsetY;
            pPreviewRobot.setTranslateX(newRobotTranslateX);
            pPreviewRobot.setTranslateY(newRobotTranslateY);

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

