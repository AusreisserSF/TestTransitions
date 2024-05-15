package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import sample.auto.fx.FieldFXCenterStageBackdropLG;
import sample.auto.fx.RobotFXCenterStageLG;

// Start with the drag-and-release code from here:
// http://java-buddy.blogspot.com/2013/07/javafx-drag-and-move-something.html#google_vignette
public class DragPreviewRobot extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Positioning is determined by the upper left corner of the robot.
        RobotFXCenterStageLG previewRobot = new RobotFXCenterStageLG(RobotFXCenterStageLG.PREVIEW_ROBOT_ID,
                14.0, 14.0, Color.GREEN,
                6.0, -6.0, 78.0,
                6.0, 6.0,
                new Point2D(36.0  * FieldFXCenterStageBackdropLG.PX_PER_INCH - ((14.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2),
                        36.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH - ((14.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2)),
                0.0);

        Line fovLineLeft = new Line(42.0  * FieldFXCenterStageBackdropLG.PX_PER_INCH, 36.0  * FieldFXCenterStageBackdropLG.PX_PER_INCH - ((14.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2),
                26.0  * FieldFXCenterStageBackdropLG.PX_PER_INCH, 16.0  * FieldFXCenterStageBackdropLG.PX_PER_INCH);
        Line fovLineRight = new Line(42.0  * FieldFXCenterStageBackdropLG.PX_PER_INCH, 36.0  * FieldFXCenterStageBackdropLG.PX_PER_INCH - ((14.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2),
                56.0  * FieldFXCenterStageBackdropLG.PX_PER_INCH, 16.0  * FieldFXCenterStageBackdropLG.PX_PER_INCH);

        new PreviewRobotDragAndRelease(previewRobot, fovLineLeft, fovLineRight);

        Group root = new Group();
        root.getChildren().addAll(previewRobot.getRobot(), fovLineLeft, fovLineRight);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.setTitle(DragPreviewRobot.class.getSimpleName());
        primaryStage.show();
    }

    private static class PreviewRobotDragAndRelease {
        private double orgSceneX, orgSceneY;
        private double orgRobotTranslateX, orgRobotTranslateY;
        private EventHandler<MouseEvent> robotGroupMouseDraggedHandler;
        private double orgFOVLineLeftTranslateX, orgFOVLineLeftTranslateY;
        private double orgFOVLineRightTranslateX, orgFOVLineRightTranslateY;
        private double orgCameraTranslateX;
        private double orgCameraTranslateY;

        //**TODO It would be ideal to redraw the FOV lines as the preview
        // robot is dragged about within the approach zone. Do this in the
        // testbed first.

        //**TODO Camera drag/release works but robot group also moves slightly.
        private PreviewRobotDragAndRelease(RobotFXCenterStageLG pPreviewRobot, Line pLineFOVLeft, Line pLineFOVRight) {

            // Get the Group that contains the actual robot.
            Group previewRobotGroup = pPreviewRobot.getRobot();

            Rectangle cameraOnRobot = (Rectangle) previewRobotGroup.lookup("#" + previewRobotGroup.getId() + "_" + RobotFXCenterStageLG.CAMERA_ON_ROBOT_ID);
            cameraOnRobot.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
                orgSceneX = mouseEvent.getSceneX();
                orgSceneY = mouseEvent.getSceneY();
                orgCameraTranslateX = cameraOnRobot.getTranslateX();
                orgCameraTranslateY = cameraOnRobot.getTranslateY();
            });

            // --- remember initial coordinates of mouse cursor and nodes
            previewRobotGroup.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
                orgSceneX = mouseEvent.getSceneX();
                orgSceneY = mouseEvent.getSceneY();

                //**TODO These are different for each Shape ... ??generalize by
                // putting into an EnumMap??
                orgRobotTranslateX = previewRobotGroup.getTranslateX();
                orgRobotTranslateY = previewRobotGroup.getTranslateY();
                orgFOVLineLeftTranslateX = pLineFOVLeft.getTranslateX();
                orgFOVLineLeftTranslateY = pLineFOVLeft.getTranslateY();
                orgFOVLineRightTranslateX = pLineFOVRight.getTranslateX();
                orgFOVLineRightTranslateY = pLineFOVRight.getTranslateY();
            });

            robotGroupMouseDraggedHandler = (MouseEvent mouseEvent) -> {
                double offsetX = mouseEvent.getSceneX() - orgSceneX;
                double offsetY = mouseEvent.getSceneY() - orgSceneY;

                // Drag the robot.
                double newRobotTranslateX = orgRobotTranslateX + offsetX;
                double newRobotTranslateY = orgRobotTranslateY + offsetY;
                previewRobotGroup.setTranslateX(newRobotTranslateX);
                previewRobotGroup.setTranslateY(newRobotTranslateY);

                // Drag the left boundary of the camera field of view.
                double newFOVLineLeftTranslateX = orgFOVLineLeftTranslateX + offsetX;
                double newFOVLineLeftTranslateY = orgFOVLineLeftTranslateY + offsetY;
                pLineFOVLeft.setTranslateX(newFOVLineLeftTranslateX);
                pLineFOVLeft.setTranslateY(newFOVLineLeftTranslateY);

                // Drag the right boundary of the camera field of view.
                double newFOVLineRightTranslateX = orgFOVLineRightTranslateX + offsetX;
                double newFOVLineRightTranslateY = orgFOVLineRightTranslateY + offsetY;
                pLineFOVRight.setTranslateX(newFOVLineRightTranslateX);
                pLineFOVRight.setTranslateY(newFOVLineRightTranslateY);
            };

            cameraOnRobot.addEventFilter(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {

                previewRobotGroup.removeEventFilter(MouseEvent.MOUSE_DRAGGED, robotGroupMouseDraggedHandler);

                double offsetX = mouseEvent.getSceneX() - orgSceneX;
                double offsetY = mouseEvent.getSceneY() - orgSceneY;

                // Drag the camera.
                double newCameraTranslateX = orgCameraTranslateX + offsetX;
                double newCameraTranslateY = orgCameraTranslateY + offsetY;
                cameraOnRobot.setTranslateX(newCameraTranslateX);
                cameraOnRobot.setTranslateY(newCameraTranslateY);
            });

            cameraOnRobot.addEventFilter(MouseEvent.MOUSE_RELEASED, (MouseEvent mouseEvent) -> {
                // restore
                previewRobotGroup.addEventFilter(MouseEvent.MOUSE_DRAGGED, robotGroupMouseDraggedHandler);
                    });

            // --- Coordinated drag of nodes calculated from mouse cursor movement
            previewRobotGroup.addEventFilter(MouseEvent.MOUSE_DRAGGED, robotGroupMouseDraggedHandler);
        }
    }

}