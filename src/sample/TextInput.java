package sample;


// From https://www.pragmaticcoding.ca/javafx/textformatter1

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.util.Optional;
import java.util.function.Predicate;

public class TextInput extends Application {
    public static final double MIN_ROBOT_WIDTH = 12.0;
    public static final double MAX_ROBOT_WIDTH = 18.0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(new TestPane(), 300, 100);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static class TestPane extends BorderPane {

        private TextField widthParameter = new TextField();
        private boolean widthParameterValid = false;
        private ObjectProperty<Double> valueProperty = new SimpleObjectProperty<>(0.0);

        public TestPane() {
            TextFormatter<Double> textFormatter = new TextFormatter<>(new DoubleStringConverter());
            textFormatter.valueProperty().bindBidirectional(valueProperty);
            widthParameter.setTextFormatter(textFormatter);

            valueProperty.addListener(new PredicateChangeListener(
                    widthP -> {
                        widthParameter.setText(widthP.toString());
                        widthParameterValid = widthP >= MIN_ROBOT_WIDTH && widthP <= MAX_ROBOT_WIDTH;
                        return widthParameterValid;
                    },
                    "The width of the robot must be between 12.0 and 18.0 inches"));

            setCenter(new VBox(10,
                    new HBox(6, new Text("TextField 1"), widthParameter))); //,
            //new HBox(6, new Text("TextField 2"), new TextField())));
        }

        // Based on this answer from Fabian --
        // https://stackoverflow.com/questions/50102818/shared-changelistener-vs-multiple-changelisteners
        private class PredicateChangeListener implements ChangeListener<Double> {
            private final Predicate<Double> changePredicate;
            private final String errorMsg;

            PredicateChangeListener(Predicate<Double> pChangePredicate, String pErrorMsg) {
                changePredicate = pChangePredicate;
                errorMsg = pErrorMsg;
            }

            @Override
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
                if (!changePredicate.test(newValue)) {
                    // Alert method
                    /*
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setHeaderText("Input not valid");
                    errorAlert.setContentText(errorMsg);
                    errorAlert.showAndWait();
                    */

                    // TextInputDialog from https://code.makery.ch/blog/javafx-dialogs-official/
                    TextInputDialog dialog = new TextInputDialog("0.0");
                    dialog.setTitle("Error Correction Dialog");
                    dialog.setHeaderText(errorMsg);
                    dialog.setContentText("Please enter a new value:");
                    Optional<String> result = dialog.showAndWait();
                    if (result.isEmpty()) {
                        System.out.println("You cancelled the dialog");
                    } else {
                        try {
                           Double.parseDouble(result.get());
                        } catch (NumberFormatException nex) {
                            System.out.println("You did not enter a valid double");
                            return;
                        }

                        widthParameter.setText(result.get());
                        System.out.println("You entered a corrected value of " + widthParameter.getText());
                    }
                } else
                    System.out.println("The original TextField passed the width filter " + newValue);
            }
        }
    }

}




