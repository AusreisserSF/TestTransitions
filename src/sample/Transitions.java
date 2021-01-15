package sample;

import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Transitions extends Application {

    //## See fix below private final ParallelTransition parallel = new ParallelTransition();
    private final SequentialTransition sequentialRobot1 = new SequentialTransition();
    private final SequentialTransition sequentialRobot2 = new SequentialTransition();

    private boolean play = true;
    private boolean pause = false;

    @Override
    public void start(final Stage pStage) {

        FieldFX fieldFX = new FieldFX();
        Pane field = fieldFX.getField();

        // Place one field boundary for testing.
        Line northBoundary = new Line(0, 0, FieldFX.FIELD_WIDTH, 0);
        northBoundary.setStrokeWidth(5.0);
        field.getChildren().add(northBoundary);

        // Place the robots on the field.
        // The first robot.
        Rectangle robotBody1 = new Rectangle(100, 500, 60, 60);
        robotBody1.setArcHeight(15);
        robotBody1.setArcWidth(15);
        robotBody1.setStroke(Color.BLACK);
        robotBody1.setFill(Color.CRIMSON);
        field.getChildren().add(robotBody1);

        robotBody1.boundsInParentProperty().addListener((observable, oldValue, newValue) -> {
            if (northBoundary.getBoundsInParent().intersects(robotBody1.getBoundsInParent())) {
                //## Was BROKEN!! when running with a ParallelTransition IllegalStateException on next line
                sequentialRobot1.stop();
                System.out.println("Collision detected");
            }
        });

        TranslateTransition translateTransition1 = new TranslateTransition();
        translateTransition1.setNode(robotBody1);
        translateTransition1.setByX(0);
        translateTransition1.setByY(-495);
        translateTransition1.setDuration(Duration.seconds(2));
        translateTransition1.setOnFinished(event -> {
            robotBody1.setLayoutX(robotBody1.getLayoutX() + robotBody1.getTranslateX());
            robotBody1.setLayoutY(robotBody1.getLayoutY() + robotBody1.getTranslateY());
            robotBody1.setTranslateX(0);
            robotBody1.setTranslateY(0);
        });
        sequentialRobot1.getChildren().add(translateTransition1);

        // The second robot.
        Rectangle robotBody2 = new Rectangle(300, 500, 60, 60);
        robotBody2.setArcHeight(15);
        robotBody2.setArcWidth(15);
        robotBody2.setStroke(Color.BLACK);
        robotBody2.setFill(Color.CYAN);

        // Is it ok to set the rotation before adding the robot to the field?  Yes.
        robotBody2.setRotate(-30.0);
        Point2D rbCoord = robotBody2.localToScene(robotBody2.getX(), robotBody2.getY());
        System.out.println( "Initial (rotated) position x " + rbCoord.getX() + ", y " + rbCoord.getY());

        field.getChildren().add(robotBody2);

        RotateTransition rotateTransition =
                new RotateTransition(Duration.seconds(1), robotBody2);
        rotateTransition.setByAngle(30.0);
        rotateTransition.setOnFinished(event -> {
            Point2D rbCoord2 = robotBody2.localToScene(robotBody2.getX(), robotBody2.getY());
            System.out.println( "Position after turn x " + rbCoord2.getX() + ", y " + rbCoord2.getY());
        });

        sequentialRobot2.getChildren().add(rotateTransition);

        TranslateTransition translateTransition2 = new TranslateTransition();
        translateTransition2.setNode(robotBody2);
        translateTransition2.setByX(0);
        translateTransition2.setByY(-400);
        translateTransition2.setDuration(Duration.seconds(4));
        translateTransition2.setOnFinished(event -> {
            robotBody2.setLayoutX(robotBody2.getLayoutX() + robotBody2.getTranslateX());
            robotBody2.setLayoutY(robotBody2.getLayoutY() + robotBody2.getTranslateY());
            robotBody2.setTranslateX(0);
            robotBody2.setTranslateY(0);
        });
        sequentialRobot2.getChildren().add(translateTransition2);

        // https://www.geeksforgeeks.org/javafx-button-with-examples/
        Button b = new Button("Play");

        // action event
        EventHandler<ActionEvent> event = e -> {
            if (play) {
                play = false;
                pause = true;
                b.setText("Pause");
                sequentialRobot1.play();
                sequentialRobot2.play();
            }

            else if (pause) {
                pause = false;
                play = true;
                b.setText("Play");
                sequentialRobot1.pause();
                sequentialRobot2.pause();
            }
        };

        // when button is pressed
        b.setOnAction(event);
        b.setLayoutX(300);
        b.setLayoutY(300);
        field.getChildren().add(b);

        //## According to the fix, do not need a ParallelTransition.
        //parallel.getChildren().addAll(sequentialRobot1, sequentialRobot2);
        //parallel.play();

        pStage.setTitle("Test Transitions");
        pStage.setScene(new Scene(field, FieldFX.FIELD_WIDTH, FieldFX.FIELD_HEIGHT, Color.GRAY));
        pStage.show();

        // Fix according to the answer from swpalmer on
        // https://stackoverflow.com/questions/64921759/javafx-sequentialtransition-illegalstateexception-cannot-stop-when-embedded-in
        //sequentialRobot1.play();
        //sequentialRobot2.play();

    }

}
