package sample;

import java.util.List;

import javafx.animation.*;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.RotateEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

// Combination of
// https://www.infoworld.com/article/2074529/javafx-2-animation--path-transitions.html
// and
// https://docs.oracle.com/javafx/2/animations/basics.htm#CJAJJAGI
public class Curves extends Application {

    public static final double ROBOT_WIDTH = 60; // pixels
    public static final double ROBOT_HEIGHT = 60;
    public static final double WHEEL_WIDTH = 10;
    public static final double WHEEL_HEIGHT = 20;
    public static final double PHONE_WIDTH = 20;
    public static final double PHONE_HEIGHT = 10;

    @Override
    public void start(final Stage pStage) {

        FieldFX fieldFX = new FieldFX();
        Pane field = fieldFX.getField();

        pStage.setTitle("JavaFX CubicCurve");
        pStage.setScene(new Scene(field, FieldFX.FIELD_WIDTH, FieldFX.FIELD_HEIGHT, Color.GRAY));
        pStage.show();

        applyAnimation(field);
    }

     private void applyAnimation(Pane pField) {

        Group robot = new Group();
        final Rectangle robotBody = new Rectangle(120, 210, ROBOT_WIDTH, ROBOT_HEIGHT);
        robotBody.setFill(Color.CRIMSON);
        robotBody.setId("robotBodyId");
        robot.getChildren().add(robotBody);

        Rectangle robotPhone = new Rectangle(120 + (ROBOT_WIDTH / 2) - (PHONE_WIDTH / 2), 210, PHONE_WIDTH, PHONE_HEIGHT);
        robotPhone.setArcHeight(5);
        robotPhone.setArcWidth(5);
        robotPhone.setFill(Color.BLACK);
        robot.getChildren().add(robotPhone);

        // clue from https://stackoverflow.com/questions/53302083/javafx-animate-along-path-parallel-to-tangent
        // But rotation shows as 270.0; I want 0.0
        robot.getTransforms().add(new Rotate(90,120 + (ROBOT_WIDTH / 2), 210 + (ROBOT_HEIGHT / 2)));
        pField.getChildren().add(robot);

        //## Attempt to track the upper left-hand corner of the rectangle.
        //## but messes up the tracking.
        //rect.setLayoutX(30.0);
        //rect.setLayoutY(30.0);

        /*
        // Draw lines at 90 degrees.
        MoveTo mt = new MoveTo();
        mt.setX(150.0f);
        mt.setY(450.0f);

        VLineTo vlt = new VLineTo(150.0);
        HLineTo hlt = new HLineTo(300.0);

        final Path path = new Path();
        path.getElements().add(mt);
        path.getElements().add(vlt);
        path.getElements().add(hlt);
        pField.getChildren().add(path);

        // Draw a line at 45 degrees.
        MoveTo mt45 = new MoveTo();
        mt45.setX(150.0f);
        mt45.setY(150.0f);
        LineTo line45 = new LineTo(250.0, 50.0);

        final Path path45 = new Path();
        path.getElements().add(mt45);
        path.getElements().add(line45);
        pField.getChildren().add(path45);
         */

         SequentialTransition seqTrans = new SequentialTransition();

         final Path cPath1 = new Path();
         MoveTo moveTo1 = new MoveTo(150, 240);
         cPath1.getElements().add(moveTo1);

         // 90 degree curve
         CubicCurveTo ccTo1 = new CubicCurveTo(150, 149.5, 150, 149.5, 240, 150);
         cPath1.getElements().add(ccTo1);

         LineTo lineTo = new LineTo(450, 250);
         cPath1.getElements().add(lineTo);

         Point2D center = robotBody.localToParent(robotBody.getX() + 0.5 * robotBody.getWidth(),
                 robotBody.getY() + 0.5 * robotBody.getHeight());

         //**TODO Not quite right because the x and y coordinates of the control points should
         // not be at the upper left of the robot, rather at its center. But this requires
         // translation.
         // see https://stackoverflow.com/questions/48624376/cubiccurve-and-cubiccurveto-in-javafx
         CubicCurveTo ccTo2 = new CubicCurveTo(490, 236, 490, 236, 300, 400);
         cPath1.getElements().add(ccTo2);

         pField.getChildren().add(cPath1);

        final PathTransition cpTrans1 = generatePathTransition(robot, cPath1);
        seqTrans.getChildren().add(cpTrans1);



        seqTrans.play();

        //translateAndRotate(group);

    }

    private Path generateCurvyPath() {

        Path path = new Path();
        MoveTo moveTo = new MoveTo(150, 240);
        path.getElements().add(moveTo);

        // 90 degree curve
        CubicCurveTo ccTo = new CubicCurveTo(150, 149.5, 150, 149.5, 240, 150);
        path.getElements().add(ccTo);

        // 45 degree curve
        //path.getElements().add(new CubicCurveTo(150, 149.5, 150, 149.5, 250, 50));
        return path;
    }


    private Path generateLineTo() {

        Path path = new Path();
        MoveTo moveTo = new MoveTo(150, 240);
        path.getElements().add(moveTo);

        // 90 degree curve
        LineTo lineTo = new LineTo(150, 150);
        path.getElements().add(lineTo);
        return path;
    }

    private PathTransition generatePathTransition(Group pRobot, final Path path) {
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(4000));
        pathTransition.setPath(path);
        pathTransition.setNode(pRobot);
        pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        pathTransition.setCycleCount(1);
        pathTransition.setAutoReverse(false);
        pathTransition.setOnFinished(event -> {
            Rectangle robotBody = (Rectangle) pRobot.lookup("#robotBodyId");
            Point2D rbCoord = robotBody.localToScene(robotBody.getX(), robotBody.getY());
            System.out.println("Position after path following x " + rbCoord.getX() + ", y " + rbCoord.getY());
            System.out.println("Rotation " + pRobot.getRotate());
        });
        return pathTransition;
    }

    private void translateAndRotate(Group pGroup) {
        final Rectangle rect2 = new Rectangle(120, 210, 60, 60);
        rect2.setFill(Color.DARKCYAN);
        pGroup.getChildren().add(rect2);

        rect2.xProperty().addListener(xValue -> {
            System.out.println("XProperty " + xValue);
        });

        PauseTransition pauseTransition = new PauseTransition(Duration.millis(2000));

        TranslateTransition translateNorth = new TranslateTransition();
        translateNorth.setNode(rect2);
        translateNorth.setByY(-90);
        translateNorth.setDuration(Duration.seconds(2));
        translateNorth.setOnFinished(event -> {
            Point2D rbCoord = rect2.localToScene(rect2.getX(), rect2.getY());
            System.out.println("Position after straight line motion x " + rbCoord.getX() + ", y " + rbCoord.getY());
        });

        RotateTransition rotate90 =
                new RotateTransition(Duration.seconds(2), rect2);
        rotate90.setByAngle(90.0);
        rotate90.setOnFinished(event -> {
            Point2D rbCoord = rect2.localToScene(rect2.getX(), rect2.getY());
            System.out.println("Position after turn x " + rbCoord.getX() + ", y " + rbCoord.getY());
            System.out.println("Rotation " + rect2.getRotate());
        });

        TranslateTransition translateWest = new TranslateTransition();
        translateWest.setNode(rect2);
        translateWest.setByX(90);
        translateWest.setDuration(Duration.seconds(2));
        translateWest.setOnFinished(event -> {
            Point2D rbCoord = rect2.localToScene(rect2.getX(), rect2.getY());
            System.out.println("Position after straight line motion x " + rbCoord.getX() + ", y " + rbCoord.getY());
        });

        SequentialTransition sequential = new SequentialTransition();
        sequential.getChildren().add(pauseTransition);
        sequential.getChildren().add(translateNorth);
        sequential.getChildren().add(rotate90);
        sequential.getChildren().add(translateWest);
        sequential.play();
    }

}