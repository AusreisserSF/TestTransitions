// From https://code.makery.ch/blog/javafx-dialogs-official/
// and
// https://www.geeksforgeeks.org/javafx-radiobutton-with-examples/

package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public class AllianceDialog extends Application {

    // launch the application 
    public void start(Stage stage) {

        VBox allianceButtons = new VBox();
        //Scene sc = new Scene(allianceButtons, 200, 200);
        //stage.setScene(sc);
        //stage.show();

// Create the custom dialog.
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Alliance selection");
        dialog.setHeaderText("Choose the alliance for the simulation");

// Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

// create radiobuttons 
        RadioButton blueButton = new RadioButton("BLUE");
        RadioButton redButton = new RadioButton("RED");

// create a toggle group for the buttons.
        ToggleGroup allianceToggleGroup = new ToggleGroup();

// add radiobuttons to toggle group 
        blueButton.setToggleGroup(allianceToggleGroup);
        redButton.setToggleGroup(allianceToggleGroup);

        allianceButtons.getChildren().addAll(blueButton, redButton);
        dialog.getDialogPane().setContent(allianceButtons);

// Convert the result to a String when the OK button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return ((RadioButton) allianceToggleGroup.getSelectedToggle()).getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(alliance -> {
            System.out.println("Alliance = " + result.get());
        });

    }
}