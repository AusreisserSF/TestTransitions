package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class SimpleTest extends Application {

    @Override
    public void start(final Stage pStage) {

        // 8/20/2023 Test StackPane from Packt Java FX 10 book.
        // this window doesn't block mouse and keyboard events
        pStage.setTitle("I'm testing something from the JavaFX 10 book");

        Pane stackPaneRoot = new StackPane();
        Rectangle red;
        stackPaneRoot.getChildren().addAll(
                new Rectangle(75, 75, Color.BLUE),
                new Rectangle(50, 50, Color.GREEN),
                red = new Rectangle(90, 90, Color.RED));
        red.toBack();

        pStage.setScene(new Scene(stackPaneRoot, 150, 150, Color.GRAY));
        pStage.show();
    }

}
