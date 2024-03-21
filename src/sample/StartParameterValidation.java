package sample;


// From https://www.pragmaticcoding.ca/javafx/textformatter1

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.DoubleStringConverter;

import java.util.EnumMap;
import java.util.function.Predicate;

//**TODO For the animation the robot's wheels are shown outside
// the body; this allows for a better representation of the camera
// and delivery device.
public class StartParameterValidation {
    public static final double MIN_ROBOT_BODY_WIDTH = 8.0;
    public static final double MAX_ROBOT_BODY_WIDTH = 18.0;

    public enum StartParameter {ROBOT_BODY_WIDTH, ROBOT_BODY_HEIGHT}

    private final EnumMap<StartParameter, StartParameterInfo> startParameters =
            new EnumMap<>(StartParameter.class);

        public StartParameterValidation(SimulatorController pSimulatorController) {

            //**TODO One listener for each start parameter.
            startParameters.put(StartParameter.ROBOT_BODY_WIDTH, new StartParameterInfo(0.0, false));
            PredicateChangeListener widthListener = (new PredicateChangeListener(
                    widthP -> {
                        // If the user doesn't enter a value for the width the animation runs
                        // but when the user closes the application window this change listener
                        // fires with a null value for I get a widthP.
                        if (widthP ==  null)
                            return true;

                        StartParameterInfo widthInfo = startParameters.get(StartParameter.ROBOT_BODY_WIDTH);
                        widthInfo.setValue(widthP);
                        boolean widthValid = widthP >= MIN_ROBOT_BODY_WIDTH && widthP <= MAX_ROBOT_BODY_WIDTH;
                        widthInfo.setValidity(widthValid);
                        return widthValid;
                    },
                    "The width of the robot must be between 8.0 and 18.0 inches"));

            validateStartParameter(pSimulatorController.robot_width_id, widthListener);
        }

        private void validateStartParameter(TextField pTextField,
                                            PredicateChangeListener pPredicateChangeListener) {

            ObjectProperty<Double> valueProperty = new SimpleObjectProperty<>(0.0);
            TextFormatter<Double> textFormatter = new TextFormatter<>(new DoubleStringConverter());
            textFormatter.valueProperty().bindBidirectional(valueProperty);
            pTextField.setTextFormatter(textFormatter);
            valueProperty.addListener(pPredicateChangeListener);
        }

        public static class StartParameterInfo {
            private double value;
            private boolean valid;

            public StartParameterInfo(double pValue, boolean pValid) {
                value = pValue;
                valid = pValid;
            }

            public void setValue(double pValue) {
                value = pValue;
            }

            public void setValidity(boolean pValid) {
                valid = pValid;
            }

            public double getValue() {
                return value;
            }

            public boolean getValidity() {
                return valid;
            }

        }

        // Based on this answer from Fabian --
        // https://stackoverflow.com/questions/50102818/shared-changelistener-vs-multiple-changelisteners
        private static class PredicateChangeListener implements ChangeListener<Double> {
            private final Predicate<Double> changePredicate;
            private final String errorMsg;

            PredicateChangeListener(Predicate<Double> pChangePredicate, String pErrorMsg) {
                changePredicate = pChangePredicate;
                errorMsg = pErrorMsg;
            }

            @Override
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
                if (!changePredicate.test(newValue)) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setHeaderText("Input not valid");
                    errorAlert.setContentText(errorMsg);
                    errorAlert.showAndWait();
                } else
                    //**TODO TEMP
                    System.out.println("Value changed -> Old Value: " + oldValue + ", New Value: " + newValue);
            }
        }

    }






