package sample;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
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
    public static final String CAMERA_FOV_LINE_LEFT_ID = "fovLineLeft";
    public static final String CAMERA_FOV_LINE_RIGHT_ID = "fovLineRight";

    private Pane field;
    private Line fovLineLeft;
    private Line fovLineRight;

    // Includes code that draws the camera's field-of-view dynamically when
    // either the robot or the camera is dragged about the field.
    @Override
    public void start(Stage primaryStage) throws Exception {

        // Make the top level a Pane, just like FieldFXCenterStageBackdrop but without the BorderPane
        // for the Start Parameters.
        //         <Pane fx:id="field" maxHeight="600.0" maxWidth="600.0" minHeight="600.0" minWidth="600.0" style="-fx-background-color: #808080;" BorderPane.alignment="CENTER_LEFT">
        field = new Pane();
        field.setMinWidth(600.0);
        field.setMinHeight(600.0);
        field.setMaxHeight(600.0);
        field.setMaxWidth(600.0);
        field.setStyle("-fx-background-color: #808080;");

        // Positioning is determined by the upper left corner of the robot.
        RobotFXCenterStageLG previewRobot = new RobotFXCenterStageLG(RobotFXCenterStageLG.PREVIEW_ROBOT_ID,
                14.0, 14.0, Color.GREEN,
                6.0, -6.0, 78.0,
                6.0, 6.0,
                new Point2D(36.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH - ((14.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2),
                        36.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH - ((14.0 * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2)),
                0.0);

        Group previewRobotGroup = previewRobot.getRobot();

        //## Line that represents the center of the three AprilTags.
        Line targetCenterLine = new Line(0, TARGET_CENTER_LINE_Y, 600, TARGET_CENTER_LINE_Y);
        targetCenterLine.setStrokeWidth(3.0);
        targetCenterLine.setStroke(Color.BLACK);

        Rectangle cameraOnRobot = (Rectangle) previewRobotGroup.lookup("#" + previewRobotGroup.getId() + "_" + RobotFXCenterStageLG.CAMERA_ON_ROBOT_ID);

        // Draw the camera FOV lines from the default position of the camera.
        drawCameraFOV(cameraOnRobot, 78.0); // for this test hardcode the FOV at 78 degrees

        // Activate drag-and-release for the robot, camera, and device.
        new PreviewRobotDragAndRelease(previewRobotGroup, cameraOnRobot);

        field.getChildren().addAll(targetCenterLine, previewRobotGroup);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(field, 600, 600));
        primaryStage.setTitle(DragPreviewRobot.class.getSimpleName());
        primaryStage.show();
    }

    private class PreviewRobotDragAndRelease {
        private double orgRobotMouseX, orgRobotMouseY;
        private double orgRobotTranslateX, orgRobotTranslateY;
        private Bounds robotBodyBounds;
        private double orgCameraMouseX, orgCameraMouseY;
        private double orgCameraTranslateX, orgCameraTranslateY;
        private Bounds cameraBounds;
        private double orgDeviceMouseX, orgDeviceMouseY;
        private double orgDeviceTranslateX, orgDeviceTranslateY;
        private Bounds deviceBounds;

        //!! See answer from jewelsea in:
        // https://stackoverflow.com/questions/34887546/javafx-check-if-the-mouse-is-on-nodes-children
        // This post contains a link to:
        // https://docs.oracle.com/javase/8/javafx/events-tutorial/processing.htm
        //!! which talks about Event Filter (event capturing phase) and Event Handlers
        // (event bubbling phase). If we use EventHandlers then the children of the
        // Group will be processed first in the event bubbling phase. Then we can
        // consume the event and prevent it from bubbling upwards.
        private PreviewRobotDragAndRelease(Group pPreviewRobot, Rectangle pCameraOnRobot) {

            // Get the robot body, which defines the limits of the camera and device.
            Rectangle robotBody = (Rectangle) pPreviewRobot.lookup("#" + pPreviewRobot.getId() + "_" + RobotFXLG.ROBOT_BODY_ID);

            pCameraOnRobot.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
                orgCameraMouseX = mouseEvent.getSceneX();
                orgCameraMouseY = mouseEvent.getSceneY();
                orgCameraTranslateX = pCameraOnRobot.getTranslateX();
                orgCameraTranslateY = pCameraOnRobot.getTranslateY();
                cameraBounds = pCameraOnRobot.getLayoutBounds();
            });

            Circle deviceOnRobot = (Circle) pPreviewRobot.lookup("#" + pPreviewRobot.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
            deviceOnRobot.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
                orgDeviceMouseX = mouseEvent.getSceneX();
                orgDeviceMouseY = mouseEvent.getSceneY();
                orgDeviceTranslateX = deviceOnRobot.getTranslateX();
                orgDeviceTranslateY = deviceOnRobot.getTranslateY();
                deviceBounds = deviceOnRobot.getLayoutBounds();
            });

            // --- remember initial coordinates of mouse cursor and nodes
            pPreviewRobot.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
                orgRobotMouseX = mouseEvent.getSceneX();
                orgRobotMouseY = mouseEvent.getSceneY();
                orgRobotTranslateX = pPreviewRobot.getTranslateX();
                orgRobotTranslateY = pPreviewRobot.getTranslateY();
                robotBodyBounds = robotBody.getLayoutBounds();
            });

            pCameraOnRobot.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
                mouseEvent.consume();

                double offsetX = mouseEvent.getSceneX() - orgCameraMouseX;
                double offsetY = mouseEvent.getSceneY() - orgCameraMouseY;

                // Drag the camera.
                double currentTranslateX = pCameraOnRobot.getTranslateX();
                double currentTranslateY = pCameraOnRobot.getTranslateY();
                double newCameraTranslateX = orgCameraTranslateX + offsetX;
                double newCameraTranslateY = orgCameraTranslateY + offsetY;
                pCameraOnRobot.setTranslateX(newCameraTranslateX);
                pCameraOnRobot.setTranslateY(newCameraTranslateY);

                // Make sure the new position of the camera is within the bounds
                // of the robot body.
                if (cameraBounds.getMinX() + newCameraTranslateX < robotBodyBounds.getMinX() ||
                        cameraBounds.getMaxX() + newCameraTranslateX > robotBodyBounds.getMaxX() ||
                        cameraBounds.getMinY() + newCameraTranslateY < robotBodyBounds.getMinY() ||
                        cameraBounds.getMaxY() + newCameraTranslateY > robotBodyBounds.getMaxY()) {

                    // Revert to the last good position.
                    pCameraOnRobot.setTranslateX(currentTranslateX);
                    pCameraOnRobot.setTranslateY(currentTranslateY);
                    return;
                }

                // Remove the current FOV lines and redraw them from the new
                // camera position.
                drawCameraFOV(pCameraOnRobot, 78.0);
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
            pPreviewRobot.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
                double offsetX = mouseEvent.getSceneX() - orgRobotMouseX;
                double offsetY = mouseEvent.getSceneY() - orgRobotMouseY;

                // Drag the robot.
                double newRobotTranslateX = orgRobotTranslateX + offsetX;
                double newRobotTranslateY = orgRobotTranslateY + offsetY;
                pPreviewRobot.setTranslateX(newRobotTranslateX);
                pPreviewRobot.setTranslateY(newRobotTranslateY);

                // Remove the current FOV lines and redraw them from the new
                // camera position.
                drawCameraFOV(pCameraOnRobot, 78.0);
            });
        }
    }

    private void drawCameraFOV(Rectangle pCamera, double pCameraFOV) {

        // Remove the current FOV lines.
        field.getChildren().removeAll(fovLineLeft, fovLineRight);

        Point2D cameraCoord = pCamera.localToScene(pCamera.getX(), pCamera.getY());
        double cameraFaceX = cameraCoord.getX() + pCamera.getWidth() / 2;
        double cameraFaceY = cameraCoord.getY();

        // Get the y coordinate of the target.
        double aprilTagCenterY = TARGET_CENTER_LINE_Y - 10.0; // a point a little higher than the target

        // Get the adjacent side of the triangle from the camera to the target AprilTag.
        double fovAdjacent = Math.abs(cameraFaceY - aprilTagCenterY);
        // tangent  = opposite / adjacent
        double halfFOVTan = Math.tan(Math.toRadians(pCameraFOV / 2));
        double halfFOVOpposite = halfFOVTan * fovAdjacent;

        fovLineLeft = new Line(cameraFaceX, cameraFaceY, cameraFaceX - halfFOVOpposite, aprilTagCenterY);
        fovLineLeft.setId(CAMERA_FOV_LINE_LEFT_ID);
        fovLineLeft.setStroke(Color.CORAL);
        fovLineLeft.getStrokeDashArray().addAll(10.0);
        fovLineLeft.setStrokeWidth(3.0);
        field.getChildren().add(fovLineLeft);

        fovLineRight = new Line(cameraFaceX, cameraFaceY, cameraFaceX + halfFOVOpposite, aprilTagCenterY);
        fovLineRight.setId(CAMERA_FOV_LINE_RIGHT_ID);
        fovLineRight.setStroke(Color.CORAL);
        fovLineRight.getStrokeDashArray().addAll(10.0);
        fovLineRight.setStrokeWidth(3.0);
        field.getChildren().add(fovLineRight);
    }

}