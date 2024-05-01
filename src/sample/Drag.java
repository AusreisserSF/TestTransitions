package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

// From https://jenkov.com/tutorials/javafx/drag-and-drop.html#pageToc

public class Drag extends Application {

    double sceneX, sceneY, layoutX, layoutY;

    public void start(Stage stage) {

        Pane root = new Pane();
        Scene sc = new Scene(root, 600, 600);
        stage.setScene(sc);
        stage.show();
        root.getChildren().addAll(getBox("green"), getBox("red"), getBox("yellow"));

    }

    private StackPane getBox(String color) {
        StackPane box = new StackPane();
        box.getChildren().add(new Label("Drag me !!"));
        box.setStyle("-fx-background-color:" + color + ";-fx-border-width:2px;-fx-border-color:black;");
        box.setPrefSize(150, 150);
        box.setMaxSize(150, 150);
        box.setMinSize(150, 150);
        box.setOnMousePressed(e -> {
            sceneX = e.getSceneX();
            sceneY = e.getSceneY();
            layoutX = box.getLayoutX();
            layoutY = box.getLayoutY();
            System.out.println(color.toUpperCase() + " Box onStart :: layoutX ::" + layoutX + ", layoutY::" + layoutY);
        });
        box.setOnMouseDragged(e -> {
            double offsetX = e.getSceneX() - sceneX;
            double offsetY = e.getSceneY() - sceneY;
            box.setTranslateX(offsetX);
            box.setTranslateY(offsetY);
            //!! The next line is crucial - see jewelsea's answer in:
            // https://stackoverflow.com/questions/45089396/javafx-how-to-overlap-a-shape-onto-another
            box.toFront();
        });
        box.setOnMouseReleased(e -> {
            // Updating the new layout positions
            box.setLayoutX(layoutX + box.getTranslateX());
            box.setLayoutY(layoutY + box.getTranslateY());

            // Resetting the translate positions
            box.setTranslateX(0);
            box.setTranslateY(0);
        });
        return box;
    }
}