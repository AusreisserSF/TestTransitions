package sample;

//## import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    private static final double FIELD_WIDTH = 600;
    private static final double FIELD_HEIGHT = 600;

    private final Pane field = new Pane();
    //## See fix below private final ParallelTransition parallel = new ParallelTransition();
    private final SequentialTransition sequentialRobot1 = new SequentialTransition();
    private final SequentialTransition sequentialRobot2 = new SequentialTransition();

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        // Place one field boundary for testing.
        Line northBoundary = new Line(0, 0, FIELD_WIDTH, 0);
        northBoundary.setStrokeWidth(5.0);
        field.getChildren().add(northBoundary);

        // Place the robots on the field.
        // The first robot.
        Rectangle robotBody1 = new Rectangle(100, 300, 60, 60);
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
        translateTransition1.setByY(-300);
        translateTransition1.setDuration(Duration.seconds(1));
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
        field.getChildren().add(robotBody2);

        TranslateTransition translateTransition2 = new TranslateTransition();
        translateTransition2.setNode(robotBody2);
        translateTransition2.setByX(0);
        translateTransition2.setByY(-400);
        translateTransition2.setDuration(Duration.seconds(2));
        translateTransition2.setOnFinished(event -> {
            robotBody2.setLayoutX(robotBody2.getLayoutX() + robotBody2.getTranslateX());
            robotBody2.setLayoutY(robotBody2.getLayoutY() + robotBody2.getTranslateY());
            robotBody2.setTranslateX(0);
            robotBody2.setTranslateY(0);
        });
        sequentialRobot2.getChildren().add(translateTransition2);

        // Fix according to the answer from swpalmer on
        // https://stackoverflow.com/questions/64921759/javafx-sequentialtransition-illegalstateexception-cannot-stop-when-embedded-in
        sequentialRobot1.play();
        sequentialRobot2.play();

        //## According to the fix, do not need a ParallelTransition.
        //parallel.getChildren().addAll(sequentialRobot1, sequentialRobot2);
        //parallel.play();

        primaryStage.setTitle("Field");
        primaryStage.setScene(new Scene(field, FIELD_WIDTH, FIELD_HEIGHT, Color.GRAY));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
