package sample;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

// Start with the drag-and-release code from here:
// http://java-buddy.blogspot.com/2013/07/javafx-drag-and-move-something.html#google_vignette
public class DragPreviewRobot extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Rectangle previewRobot = new Rectangle(200, 200, 100, 100);
        Line fovLineLeft = new Line(250, 200, 150, 100);
        Line fovLineRight = new Line(250, 200, 350, 100);

        new PreviewRobotDragAndRelease(previewRobot, fovLineLeft, fovLineRight);

        Group root = new Group();
        root.getChildren().addAll(previewRobot, fovLineLeft, fovLineRight);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.setTitle(DragPreviewRobot.class.getSimpleName());
        primaryStage.show();
    }

    private static class PreviewRobotDragAndRelease {
        private double orgSceneX, orgSceneY;
        private double orgRobotTranslateX, orgRobotTranslateY;
        private double orgFOVLineLeftTranslateX, orgFOVLineLeftTranslateY;
        private double orgFOVLineRightTranslateX, orgFOVLineRightTranslateY;

        //**TODO It would be ideal to redraw the FOV lines as the preview
        // robot is dragged about within the approach zone. Do this in the
        // testbed first.

        private PreviewRobotDragAndRelease(Rectangle pRobotRect, Line pFOVLineLeft, Line pFOVLineRight) {

            // --- remember initial coordinates of mouse cursor and nodes
            pRobotRect.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
                orgSceneX = mouseEvent.getSceneX();
                orgSceneY = mouseEvent.getSceneY();

                //**TODO These are different for each Shape ... generalize by
                // putting into an EnumMap??
                orgRobotTranslateX = pRobotRect.getTranslateX();
                orgRobotTranslateY = pRobotRect.getTranslateY();
                orgFOVLineLeftTranslateX = pFOVLineLeft.getTranslateX();
                orgFOVLineLeftTranslateY = pFOVLineLeft.getTranslateY();
                orgFOVLineRightTranslateX = pFOVLineRight.getTranslateX();
                orgFOVLineRightTranslateY = pFOVLineRight.getTranslateY();
            });

            // --- Coordinated drag of nodes calculated from mouse cursor movement
            pRobotRect.addEventFilter(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
                double offsetX = mouseEvent.getSceneX() - orgSceneX;
                double offsetY = mouseEvent.getSceneY() - orgSceneY;

                // Drag the robot.
                double newRobotTranslateX = orgRobotTranslateX + offsetX;
                double newRobotTranslateY = orgRobotTranslateY + offsetY;
                pRobotRect.setTranslateX(newRobotTranslateX);
                pRobotRect.setTranslateY(newRobotTranslateY);

                // Drag the left boundary of the camera field of view.
                double newFOVLineLeftTranslateX = orgFOVLineLeftTranslateX + offsetX;
                double newFOVLineLeftTranslateY = orgFOVLineLeftTranslateY + offsetY;
                pFOVLineLeft.setTranslateX(newFOVLineLeftTranslateX);
                pFOVLineLeft.setTranslateY(newFOVLineLeftTranslateY);

                // Drag the right boundary of the camera field of view.
                double newFOVLineRightTranslateX = orgFOVLineRightTranslateX + offsetX;
                double newFOVLineRightTranslateY = orgFOVLineRightTranslateY + offsetY;
                pFOVLineRight.setTranslateX(newFOVLineRightTranslateX);
                pFOVLineRight.setTranslateY(newFOVLineRightTranslateY);
            });
        }

    }

}