package sample;


// From https://www.pragmaticcoding.ca/javafx/textformatter1

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

public class TextInput extends Application {
    private final ObjectProperty<Double> valueProperty = new SimpleObjectProperty<>(0.0);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(new TestPane(), 300, 100);
        valueProperty.addListener(((observable, oldValue, newValue) -> {
            System.out.println("Value changed -> Old Value: " + oldValue + ", New Value: " + newValue);

            if (newValue < 0.0) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Input not valid");
                errorAlert.setContentText("The width of the robot must be between 8.0 and 18.0 inches");
                errorAlert.showAndWait();
            }
        }));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public class TestPane extends BorderPane {
        public TestPane() {
            TextField textField = new TextField();
            TextFormatter<Double> textFormatter = new TextFormatter<>(new DoubleStringConverter());
            textFormatter.valueProperty().bindBidirectional(valueProperty);
            textField.setTextFormatter(textFormatter);
            setCenter(new VBox(10,
                    new HBox(6, new Text("TextField 1"), textField),
                    new HBox(6, new Text("TextField 2"), new TextField())));
        }
    }

    public static class PositiveDoubleStringConverter extends DoubleStringConverter {

        @Override
        public Double fromString(String value) {
            double result = super.fromString(value);
            if (result < 0) {
                throw new RuntimeException("Negative number");
            }
            return result;
        }

        @Override
        public String toString(Double value) {
            if (value < 0) {
                return "0";
            }
            return super.toString(value);
        }

    }

}


