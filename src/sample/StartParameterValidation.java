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
import java.util.Optional;
import java.util.function.Predicate;

//**TODO For the animation the robot's wheels are shown outside
// the body; this allows for a better representation of the camera
// and delivery device.
public class StartParameterValidation {
    public static final double MIN_ROBOT_BODY_DIMENSION = 12.0;
    public static final double MAX_ROBOT_BODY_DIMENSION = 18.0;

    //**TODO There must be limits on the approach position - neither
    // too close nor too far left or right (camera field of view?)
    // nor too far from the backdrop.
    public enum StartParameter {ROBOT_BODY_WIDTH, ROBOT_BODY_HEIGHT,
    CAMERA_CENTER_FROM_ROBOT_CENTER_ID, CAMERA_OFFSET_FROM_ROBOT_CENTER_ID,
    DEVICE_CENTER_FROM_ROBOT_CENTER_ID, DEVICE_OFFSET_FROM_ROBOT_CENTER_ID,
    POSITION_X_ID, POSITION_Y_ID}

    private final EnumMap<StartParameter, StartParameterInfo> startParameters =
            new EnumMap<>(StartParameter.class);

        public StartParameterValidation(SimulatorController pSimulatorController) {

            //**TODO One listener for each start parameter with a range of double values.

            // Robot width.
            startParameters.put(StartParameter.ROBOT_BODY_WIDTH, new StartParameterInfo(0.0, false));
            PredicateChangeListener widthListener = (new PredicateChangeListener(
                    widthP -> {
                        // If the user doesn't enter a value for the width the animation runs
                        // but when the user closes the application window this change listener
                        // fires with a null value for widthP.
                        if (widthP ==  null)
                            return true;

                        StartParameterInfo widthInfo = startParameters.get(StartParameter.ROBOT_BODY_WIDTH);
                        widthInfo.setParameterValue(widthP);
                        boolean widthValid = widthP >= MIN_ROBOT_BODY_DIMENSION && widthP <= MAX_ROBOT_BODY_DIMENSION;
                        widthInfo.setValidity(widthValid);
                        return widthValid;
                    },
                    "The width of the robot must be between " +  MIN_ROBOT_BODY_DIMENSION + " and " + MAX_ROBOT_BODY_DIMENSION));

            validateStartParameter(pSimulatorController.robot_width_id, widthListener);

            // Robot height.
            startParameters.put(StartParameter.ROBOT_BODY_HEIGHT, new StartParameterInfo(0.0, false));
            PredicateChangeListener heightListener = (new PredicateChangeListener(
                    heightP -> {
                        // If the user doesn't enter a value for the height the animation runs
                        // but when the user closes the application window this change listener
                        // fires with a null value for heightP.
                        if (heightP ==  null)
                            return true;

                        StartParameterInfo heightInfo = startParameters.get(StartParameter.ROBOT_BODY_HEIGHT);
                        heightInfo.setParameterValue(heightP);
                        boolean heightValid = heightP >= MIN_ROBOT_BODY_DIMENSION && heightP <= MAX_ROBOT_BODY_DIMENSION;
                        heightInfo.setValidity(heightValid);
                        return heightValid;
                    },
                    "The height of the robot must be between " +  MIN_ROBOT_BODY_DIMENSION + " and " + MAX_ROBOT_BODY_DIMENSION));

            validateStartParameter(pSimulatorController.robot_height_id, heightListener);

            //     CAMERA_CENTER_FROM_ROBOT_CENTER_ID
            startParameters.put(StartParameter.CAMERA_CENTER_FROM_ROBOT_CENTER_ID, new StartParameterInfo(0.0, true));
            // constraint - camera left or right edge may be no more than half the width of the robot from the center
            // the edge depends on the sign of the parameter

            //     CAMERA_OFFSET_FROM_ROBOT_CENTER_ID
            startParameters.put(StartParameter.CAMERA_OFFSET_FROM_ROBOT_CENTER_ID, new StartParameterInfo(0.0, true));
            // constraint - camera top or bottom edge may be no more than half the height of the robot from the center
            // the edge depends on the sign of the parameter
        }

        //**TODO What if the parameter is not valid?
        public double getStartParameter(StartParameter pSelectedParameter) {
            return startParameters.get(pSelectedParameter).getParameterValue();
        }

        public boolean allStartParametersValid() {
            boolean allValid = true;
            Optional<StartParameterInfo> invalidEntry = startParameters.values().stream()
                    .filter(e -> !e.getValidity())
                    .findAny();
            return invalidEntry.isEmpty(); // returns true if there are no invalid entries
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
            private double parameterValue;
            private boolean valid;

            public StartParameterInfo(double pParameterValue, boolean pValid) {
                parameterValue = pParameterValue;
                valid = pValid;
            }

            public void setParameterValue(double pValue) {
                parameterValue = pValue;
            }

            public void setValidity(boolean pValid) {
                valid = pValid;
            }

            public double getParameterValue() {
                return parameterValue;
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






