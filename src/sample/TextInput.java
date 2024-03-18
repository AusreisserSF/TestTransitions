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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class TextInput extends Application {
    private final ObjectProperty<Double> valueProperty = new SimpleObjectProperty<>(0.0);

    public static final double MIN_ROBOT_WIDTH = 8.0;
    public static final double MAX_ROBOT_WIDTH = 18.0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(new TestPane(), 300, 100);
        /*
        valueProperty.addListener(((observable, oldValue, newValue) -> {
            System.out.println("Value changed -> Old Value: " + oldValue + ", New Value: " + newValue);

            if (newValue < 0.0) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Input not valid");
                errorAlert.setContentText("The width of the robot must be between 8.0 and 18.0 inches");
                errorAlert.showAndWait();
            }
        }));

         */
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public class TestPane extends BorderPane {
        public TestPane() {
            TextField textField = new TextField();

            TextFormatter<Double> textFormatter = new TextFormatter<>(new DoubleStringConverter());
            textFormatter.valueProperty().bindBidirectional(valueProperty);
            textField.setTextFormatter(textFormatter);

            valueProperty.addListener(new PredicateChangeListener(
                    parameter -> parameter >= MIN_ROBOT_WIDTH && parameter <= MAX_ROBOT_WIDTH));


            setCenter(new VBox(10,
                    new HBox(6, new Text("TextField 1"), textField),
                    new HBox(6, new Text("TextField 2"), new TextField())));

            //double validatedParameter =
            //validateRobotStartParameter(textField, (Double parameter) -> parameter >= MIN_ROBOT_WIDTH && parameter <= MAX_ROBOT_WIDTH);
            //System.out.println("Validated parameter: " + validatedParameter);
        }


        // Here's another one from Fabian --
// https://stackoverflow.com/questions/50102818/shared-changelistener-vs-multiple-changelisteners

        private class PredicateChangeListener implements ChangeListener<Double> {
            private final Predicate<Double> changePredicate;

            PredicateChangeListener(Predicate<Double> pChangePredicate) {
                changePredicate = pChangePredicate;
            }

            @Override
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {

                if (!changePredicate.test(newValue)) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setHeaderText("Input not valid");
                    errorAlert.setContentText("The width of the robot must be between 8.0 and 18.0 inches");
                    errorAlert.showAndWait();
                } else
                    System.out.println("Value changed -> Old Value: " + oldValue + ", New Value: " + newValue);

            }

        }


        //**TODO This just doesn't work because it returns 0.0 on the first call.
        private double validateRobotStartParameter(TextField pTextField, Predicate<Double> pTest) {
            AtomicReference<Double> retVal = new AtomicReference<>((double) 0);
            ObjectProperty<Double> valueProperty = new SimpleObjectProperty<>(0.0);
            TextFormatter<Double> textFormatter = new TextFormatter<>(new DoubleStringConverter());
            textFormatter.valueProperty().bindBidirectional(valueProperty);
            pTextField.setTextFormatter(textFormatter);
            valueProperty.addListener(((observable, oldValue, newValue) -> {
                System.out.println("Value changed -> Old Value: " + oldValue + ", New Value: " + newValue);

                if (!pTest.test(newValue)) {
                    retVal.set(0.0);
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setHeaderText("Input not valid");
                    errorAlert.setContentText("The width of the robot must be between 8.0 and 18.0 inches");
                    errorAlert.showAndWait();
                } else retVal.set(newValue);
            }));

            return retVal.get();
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


