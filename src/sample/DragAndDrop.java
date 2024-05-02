package sample;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

// Based on https://docs.oracle.com/javafx/2/drag_drop/HelloDragAndDrop.java.html
public class DragAndDrop extends Application {
    @Override public void start(Stage stage) {
        stage.setTitle("Hello Drag And Drop");

        Group root = new Group();
        Scene scene = new Scene(root, 400, 200);
        scene.setFill(Color.LIGHTGREEN);

        Rectangle sourceRect = new Rectangle(50, 50, 50, 50);
        sourceRect.setFill(Color.FUCHSIA);
        //Text sourceText = new Text("DRAG ME");
        //StackPane sourceStack = new StackPane();
        //sourceStack.getChildren().addAll(sourceRect, sourceText);

        Rectangle targetRect = new Rectangle(250, 50, 100, 100);
        targetRect.setFill(Color.TURQUOISE);
       // Text targetText = new Text("DROP HERE");
        //StackPane targetStack = new StackPane();
        //targetStack.getChildren().addAll(targetRect, targetText);

        sourceRect.setOnDragDetected(event -> {
            /* drag was detected, start drag-and-drop gesture*/
            System.out.println("onDragDetected");

            /* allow any transfer mode */
            Dragboard db = sourceRect.startDragAndDrop(TransferMode.ANY);

            /* put a string on dragboard */
            ClipboardContent content = new ClipboardContent();
            content.putString("Robot");
            db.setContent(content);
            event.consume();
        });

        targetRect.setOnDragOver(event -> {
            /* data is dragged over the target */
            System.out.println("onDragOver");

            /* accept it only if it is  not dragged from the same node
             * and if it has a string data */
            if (event.getGestureSource() != targetRect &&
                    event.getDragboard().hasString()) {
                /* allow for both copying and moving, whatever user chooses */
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            event.consume();
        });

        targetRect.setOnDragEntered(event -> {
            /* the drag-and-drop gesture entered the target */
            System.out.println("onDragEntered");
            /* show to the user that it is an actual gesture target */
            if (event.getGestureSource() != targetRect &&
                    event.getDragboard().hasString()) {
               // targetRect.setFill(Color.GREEN);
            }

            event.consume();
        });

        targetRect.setOnDragExited(event -> {
            /* mouse moved away, remove the graphical cues */
            //targetRect.setFill(Color.BLACK);

            event.consume();
        });

        targetRect.setOnDragDropped(event -> {
            /* data dropped */
            System.out.println("onDragDropped");
            /* if there is a string data on dragboard, read it and use it */
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                //targetText.setText(db.getString());
                success = true;
            }
            /* let the source know whether the string was successfully
             * transferred and used */
            event.setDropCompleted(success);

            event.consume();
        });

        targetRect.setOnDragDone(event -> {
            /* the drag-and-drop gesture ended */
            System.out.println("onDragDone");
            /* if the data was successfully moved, clear it */
            if (event.getTransferMode() == TransferMode.MOVE) {
                //sourceText.setText("");
            }

            event.consume();
        });

        root.getChildren().add(sourceRect);
        root.getChildren().add(targetRect);
        stage.setScene(scene);
        stage.show();
    }
}
