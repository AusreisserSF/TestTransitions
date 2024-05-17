package sample;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import sample.auto.fx.FieldFXCenterStageBackdropLG;
import sample.auto.fx.RobotFXCenterStageLG;
import sample.auto.fx.RobotFXLG;

// Start with the drag-and-release code from here:
// http://java-buddy.blogspot.com/2013/07/javafx-drag-and-move-something.html#google_vignette
public class DragPreviewRobot extends Application {

    private static final double TARGET_CENTER_LINE_Y = 100;
    Line fovLineLeft;
    Line fovLineRight;

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Positioning is determined by the upper left corner of the robot.
        RobotFXCenterStageLG previewRobot = new RobotFXCenterStageLG(RobotFXCenterStageLG.PREVIEW_ROBOT_ID,
                14.0, 14.0, Color.GREEN,
                6.0, -6.0, 78.0,
                6.0, 6.0,
                new Point2D(36.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH - ((14.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2),
                        36.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH - ((14.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2)),
                0.0);

        //**TODO Draw FOV lines dynamically from the camera to a point just beyond the target center line.
        Line targetCenterLine = new Line(0, TARGET_CENTER_LINE_Y, 600, TARGET_CENTER_LINE_Y);
        targetCenterLine.setStrokeWidth(3.0);
        targetCenterLine.setStroke(Color.BLACK);

        // Draw the camera FOV lines from the default position of the camera.
        //**TODO The default lies should show the 78-degree FOV from the camera face
        // to points just above the target center line.

        fovLineLeft = new Line(42.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH, 36.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH - ((14.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2),
                26.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH, 16.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH);
        fovLineLeft.setId("cameraFOVLineLeft");
        fovLineRight = new Line(42.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH, 36.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH - ((14.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2),
                56.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH, 16.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH);
        fovLineRight.setId("cameraFOVLineRight");

        new PreviewRobotDragAndRelease(previewRobot);

        Group root = new Group();
        root.getChildren().addAll(targetCenterLine, previewRobot.getRobot(), fovLineLeft, fovLineRight);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.setTitle(DragPreviewRobot.class.getSimpleName());
        primaryStage.show();
    }

    private class PreviewRobotDragAndRelease {
        private double orgRobotMouseX, orgRobotMouseY;
        private double orgRobotTranslateX, orgRobotTranslateY;
        private Bounds robotBodyBounds;
        private double orgFOVLineLeftTranslateX, orgFOVLineLeftTranslateY;
        private double orgFOVLineRightTranslateX, orgFOVLineRightTranslateY;
        private double orgCameraMouseX, orgCameraMouseY;
        private double orgCameraTranslateX, orgCameraTranslateY;
        private Bounds cameraBounds;
        private double orgDeviceMouseX, orgDeviceMouseY;
        private double orgDeviceTranslateX, orgDeviceTranslateY;
        private Bounds deviceBounds;

        //**TODO It would be ideal to redraw the FOV lines as the preview
        // robot is dragged about within the approach zone. Do this in the
        // testbed first.

        //!! See answer from jewelsea in:
        // https://stackoverflow.com/questions/34887546/javafx-check-if-the-mouse-is-on-nodes-children
        // This post contains a link to:
        // https://docs.oracle.com/javase/8/javafx/events-tutorial/processing.htm
        //!! which talks about Event Filter (event capturing phase) and Event Handlers
        // (event bubbling phase). If we use EventHandlers then the children of the
        // Group will be processed first in the event bubbling phase. Then we can
        // consume the event and prevent it from bubbling upwards.
        private PreviewRobotDragAndRelease(RobotFXCenterStageLG pPreviewRobot) {

            // Get the Group that contains the actual robot.
            Group previewRobotGroup = pPreviewRobot.getRobot();
            Rectangle robotBody = (Rectangle) previewRobotGroup.lookup("#" + previewRobotGroup.getId() + "_" + RobotFXLG.ROBOT_BODY_ID);

            Rectangle cameraOnRobot = (Rectangle) previewRobotGroup.lookup("#" + previewRobotGroup.getId() + "_" + RobotFXCenterStageLG.CAMERA_ON_ROBOT_ID);
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
                orgFOVLineLeftTranslateX = fovLineLeft.getTranslateX();
                orgFOVLineLeftTranslateY = fovLineLeft.getTranslateY();
                orgFOVLineRightTranslateX = fovLineRight.getTranslateX();
                orgFOVLineRightTranslateY = fovLineRight.getTranslateY();
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

                //**TODO Remove the current FOV lines and redraw them from the new
                // camera position.
            });

            cameraOnRobot.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent mouseEvent) -> {
                //**TODO Redraw FOV lines on mouse release?
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
                }
            });

            // --- Coordinated drag of nodes calculated from mouse cursor movement
            previewRobotGroup.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
                double offsetX = mouseEvent.getSceneX() - orgRobotMouseX;
                double offsetY = mouseEvent.getSceneY() - orgRobotMouseY;

                // Drag the robot.
                double newRobotTranslateX = orgRobotTranslateX + offsetX;
                double newRobotTranslateY = orgRobotTranslateY + offsetY;
                previewRobotGroup.setTranslateX(newRobotTranslateX);
                previewRobotGroup.setTranslateY(newRobotTranslateY);

                //**TODO Redraw FOV lines as the *robot* is dragged.
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

        //**TODO Return Pair<Point2D, Point2D> of the coordinates of the
        // endpoints of the lines that show the recalculated boundaries
        // of the camera FOV.
        private void recalculateFOVPosition(Rectangle pCamera, double pCameraFov) {

        }
    }

}