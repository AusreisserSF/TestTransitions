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

        Line fovLineLeft = new Line(42.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH, 36.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH - ((14.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2),
                26.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH, 16.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH);
        Line fovLineRight = new Line(42.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH, 36.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH - ((14.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2),
                56.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH, 16.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH);

        new PreviewRobotDragAndRelease(previewRobot, fovLineLeft, fovLineRight);

        Group root = new Group();
        root.getChildren().addAll(previewRobot.getRobot(), fovLineLeft, fovLineRight);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.setTitle(DragPreviewRobot.class.getSimpleName());
        primaryStage.show();
    }

    private static class PreviewRobotDragAndRelease {
        private double orgRobotMouseX, orgRobotMouseY;
        private double orgRobotTranslateX, orgRobotTranslateY;
        private double orgFOVLineLeftTranslateX, orgFOVLineLeftTranslateY;
        private double orgFOVLineRightTranslateX, orgFOVLineRightTranslateY;
        private double orgCameraMouseX, orgCameraMouseY;
        private double orgCameraTranslateX, orgCameraTranslateY;
        private double orgDeviceMouseX, orgDeviceMouseY;
        private double orgDeviceTranslateX, orgDeviceTranslateY;

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
        private PreviewRobotDragAndRelease(RobotFXCenterStageLG pPreviewRobot, Line pLineFOVLeft, Line pLineFOVRight) {

            // Get the Group that contains the actual robot.
            Group previewRobotGroup = pPreviewRobot.getRobot();
            Rectangle robotBody = (Rectangle) previewRobotGroup.lookup("#" + previewRobotGroup.getId() + "_" + RobotFXLG.ROBOT_BODY_ID);

            Rectangle cameraOnRobot = (Rectangle) previewRobotGroup.lookup("#" + previewRobotGroup.getId() + "_" + RobotFXCenterStageLG.CAMERA_ON_ROBOT_ID);
            cameraOnRobot.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
                orgCameraMouseX = mouseEvent.getSceneX();
                orgCameraMouseY = mouseEvent.getSceneY();
                orgCameraTranslateX = cameraOnRobot.getTranslateX();
                orgCameraTranslateY = cameraOnRobot.getTranslateY();
            });

            Circle deviceOnRobot = (Circle) previewRobotGroup.lookup("#" + previewRobotGroup.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
            deviceOnRobot.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
                orgDeviceMouseX = mouseEvent.getSceneX();
                orgDeviceMouseY = mouseEvent.getSceneY();
                orgDeviceTranslateX = deviceOnRobot.getTranslateX();
                orgDeviceTranslateY = deviceOnRobot.getTranslateY();
            });

            // --- remember initial coordinates of mouse cursor and nodes
            previewRobotGroup.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
                orgRobotMouseX = mouseEvent.getSceneX();
                orgRobotMouseY = mouseEvent.getSceneY();
                orgRobotTranslateX = previewRobotGroup.getTranslateX();
                orgRobotTranslateY = previewRobotGroup.getTranslateY();
                orgFOVLineLeftTranslateX = pLineFOVLeft.getTranslateX();
                orgFOVLineLeftTranslateY = pLineFOVLeft.getTranslateY();
                orgFOVLineRightTranslateX = pLineFOVRight.getTranslateX();
                orgFOVLineRightTranslateY = pLineFOVRight.getTranslateY();
            });

            cameraOnRobot.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
                mouseEvent.consume();

                double offsetX = mouseEvent.getSceneX() - orgCameraMouseX;
                double offsetY = mouseEvent.getSceneY() - orgCameraMouseY;

                // Drag the camera.
                double currentTranslateX = cameraOnRobot.getTranslateX();
                System.out.println("currentTranslateX " + currentTranslateX); //**TODO TEMP

                double currentTranslateY = cameraOnRobot.getTranslateY();

                //**TODO newCameraTranslate is relative to its original position.
                // I want the comparison to be absolute. But watch out because
                // the dragging of the camera is ok.
                double newCameraTranslateX = orgCameraTranslateX + offsetX;
                System.out.println("newCameraTranslateX " + newCameraTranslateX); //**TODO TEMP

                double newCameraTranslateY = orgCameraTranslateY + offsetY;
                cameraOnRobot.setTranslateX(newCameraTranslateX);
                cameraOnRobot.setTranslateY(newCameraTranslateY);

                // Make sure the new position of the camera is within the bounds
                // of the approach zone.
                Bounds cameraBounds = cameraOnRobot.getLayoutBounds();
                Bounds robotBodyBounds = robotBody.getLayoutBounds();

                System.out.println("camera bounds max X " + cameraBounds.getMaxX()); //**TODO TEMP
                System.out.println("robot bounds max X " + robotBodyBounds.getMaxX()); //**TODO TEMP

                if (cameraBounds.getMinX() < robotBodyBounds.getMinX() ||
                        cameraBounds.getMaxX() > robotBodyBounds.getMaxX() ||
                        cameraBounds.getMinY() < robotBodyBounds.getMinY() ||
                        cameraBounds.getMaxY() > robotBodyBounds.getMaxY()) {

                    // Revert to the last good position.
                    cameraOnRobot.setTranslateX(currentTranslateX);
                    cameraOnRobot.setTranslateY(currentTranslateY);
                    return;
                }
            });

            deviceOnRobot.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
                mouseEvent.consume();

                double offsetX = mouseEvent.getSceneX() - orgDeviceMouseX;
                double offsetY = mouseEvent.getSceneY() - orgDeviceMouseY;

                // Drag the device.
                double newCameraTranslateX = orgDeviceTranslateX + offsetX;
                double newCameraTranslateY = orgDeviceTranslateY + offsetY;
                deviceOnRobot.setTranslateX(newCameraTranslateX);
                deviceOnRobot.setTranslateY(newCameraTranslateY);
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
            });
        }
    }

}