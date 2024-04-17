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

    public enum StartParameter {
        ROBOT_WIDTH, ROBOT_HEIGHT,
        CAMERA_CENTER_FROM_ROBOT_CENTER, CAMERA_OFFSET_FROM_ROBOT_CENTER,
        DEVICE_CENTER_FROM_ROBOT_CENTER, DEVICE_OFFSET_FROM_ROBOT_CENTER,
        ROBOT_POSITION_AT_BACKDROP_X, ROBOT_POSITION_AT_BACKDROP_Y
    }

    private final EnumMap<StartParameter, StartParameterInfo> startParameters =
            new EnumMap<>(StartParameter.class);

    public StartParameterValidation(SimulatorController pSimulatorController) {

        // Create one listener for each start parameter that takes a range of double values.
        // Robot width.
        startParameters.put(StartParameter.ROBOT_WIDTH, new StartParameterInfo(0.0, false));
        PredicateChangeListener widthListener = (new PredicateChangeListener(
                widthP -> {
                    // If the user doesn't enter a value for the width the animation runs
                    // but when the user closes the application window this change listener
                    // fires with a null value for widthP.
                    if (widthP == null)
                        return true;

                    StartParameterInfo widthInfo = startParameters.get(StartParameter.ROBOT_WIDTH);
                    widthInfo.setParameterValue(widthP);
                    boolean widthValid = widthP >= MIN_ROBOT_BODY_DIMENSION && widthP <= MAX_ROBOT_BODY_DIMENSION;
                    widthInfo.setValidity(widthValid);
                    return widthValid;
                },
                "The width of the robot must be between " + MIN_ROBOT_BODY_DIMENSION + " and " + MAX_ROBOT_BODY_DIMENSION));

        validateStartParameter(pSimulatorController.robot_width, widthListener);

        // Robot height.
        startParameters.put(StartParameter.ROBOT_HEIGHT, new StartParameterInfo(0.0, false));
        PredicateChangeListener heightListener = (new PredicateChangeListener(
                heightP -> {
                    // If the user doesn't enter a value for the height the animation runs
                    // but when the user closes the application window this change listener
                    // fires with a null value for heightP.
                    if (heightP == null)
                        return true;

                    StartParameterInfo heightInfo = startParameters.get(StartParameter.ROBOT_HEIGHT);
                    heightInfo.setParameterValue(heightP);
                    boolean heightValid = heightP >= MIN_ROBOT_BODY_DIMENSION && heightP <= MAX_ROBOT_BODY_DIMENSION;
                    heightInfo.setValidity(heightValid);
                    return heightValid;
                },
                "The height of the robot must be between " + MIN_ROBOT_BODY_DIMENSION + " and " + MAX_ROBOT_BODY_DIMENSION));

        validateStartParameter(pSimulatorController.robot_height, heightListener);

        //     CAMERA_CENTER_FROM_ROBOT_CENTER_ID
        startParameters.put(StartParameter.CAMERA_CENTER_FROM_ROBOT_CENTER, new StartParameterInfo(0.0, true));
        // constraint - camera top or bottom edge may be no more than half the height of the robot from the center
        // the edge depends on the sign of the parameter.

        PredicateChangeListener cameraCenterListener = (new PredicateChangeListener(
                cameraCenterP -> {
                    // If the user doesn't enter a value the animation runs but when the
                    // user closes the application window this change listener fires with
                    // a null value for cameraCenterP.
                    if (cameraCenterP == null)
                        return true;

                    // Make sure that the robot's height has already been set.
                    StartParameterInfo heightInfo = startParameters.get(StartParameter.ROBOT_HEIGHT);

                    StartParameterInfo cameraCenterInfo = startParameters.get(StartParameter.CAMERA_CENTER_FROM_ROBOT_CENTER);
                    cameraCenterInfo.setParameterValue(cameraCenterP);
                    boolean cameraCenterValid = (heightInfo != null && heightInfo.getValidity() &&
                            Math.abs(cameraCenterInfo.getParameterValue()) < (heightInfo.getParameterValue() / 2));
                    cameraCenterInfo.setValidity(cameraCenterValid);
                    return cameraCenterValid;
                },
                "The fore/aft distance from camera center to robot center must be less than 1/2 the height of the robot"));

        validateStartParameter(pSimulatorController.camera_center_from_robot_center, cameraCenterListener);


        //     CAMERA_OFFSET_FROM_ROBOT_CENTER_ID
        startParameters.put(StartParameter.CAMERA_OFFSET_FROM_ROBOT_CENTER, new StartParameterInfo(0.0, true));
        // constraint - camera left or right edge may be no more than half the width of the robot from the center
        // the edge depends on the sign of the parameter.
        PredicateChangeListener cameraOffsetListener = (new PredicateChangeListener(
                cameraOffsetP -> {
                    // If the user doesn't enter a value the animation runs but when the
                    // user closes the application window this change listener fires with
                    // a null value for cameraOffsetP.
                    if (cameraOffsetP == null)
                        return true;

                    // Make sure that the robot's width has already been set.
                    StartParameterInfo widthInfo = startParameters.get(StartParameter.ROBOT_WIDTH);

                    StartParameterInfo cameraOffsetInfo = startParameters.get(StartParameter.CAMERA_OFFSET_FROM_ROBOT_CENTER);
                    cameraOffsetInfo.setParameterValue(cameraOffsetP);
                    boolean cameraOffsetValid = (widthInfo != null && widthInfo.getValidity() &&
                            Math.abs(cameraOffsetInfo.getParameterValue()) < (widthInfo.getParameterValue() / 2));
                    cameraOffsetInfo.setValidity(cameraOffsetValid);
                    return cameraOffsetValid;
                },
                "The left/right distance from camera center to robot center must be less than 1/2 the width of the robot"));

        validateStartParameter(pSimulatorController.camera_offset_from_robot_center, cameraOffsetListener);

        //    DEVICE_CENTER_FROM_ROBOT_CENTER_ID
        startParameters.put(StartParameter.DEVICE_CENTER_FROM_ROBOT_CENTER, new StartParameterInfo(0.0, true));
        // constraint - device top or bottom edge may be no more than half the height of the robot from the center
        // the edge depends on the sign of the parameter.
        PredicateChangeListener deviceCenterListener = (new PredicateChangeListener(
                deviceCenterP -> {
                    // If the user doesn't enter a value the animation runs but when the
                    // user closes the application window this change listener fires with
                    // a null value for deviceCenterP.
                    if (deviceCenterP == null)
                        return true;

                    // Make sure that the robot's height has already been set.
                    StartParameterInfo heightInfo = startParameters.get(StartParameter.ROBOT_HEIGHT);

                    StartParameterInfo deviceCenterInfo = startParameters.get(StartParameter.DEVICE_CENTER_FROM_ROBOT_CENTER);
                    deviceCenterInfo.setParameterValue(deviceCenterP);
                    boolean deviceCenterValid = (heightInfo != null && heightInfo.getValidity() &&
                            Math.abs(deviceCenterInfo.getParameterValue()) < (heightInfo.getParameterValue() / 2));
                    deviceCenterInfo.setValidity(deviceCenterValid);
                    return deviceCenterValid;
                },
                "The fore/aft distance from device center to robot center must be less than 1/2 the height of the robot"));

        validateStartParameter(pSimulatorController.device_center_from_robot_center, deviceCenterListener);

        //    DEVICE_OFFSET_FROM_ROBOT_CENTER_ID
        startParameters.put(StartParameter.DEVICE_OFFSET_FROM_ROBOT_CENTER, new StartParameterInfo(0.0, true));
        // constraint - device left or right edge may be no more than half the width of the robot from the center
        // the edge depends on the sign of the parameter.
        PredicateChangeListener deviceOffsetListener = (new PredicateChangeListener(
                deviceOffsetP -> {
                    // If the user doesn't enter a value the animation runs but when the
                    // user closes the application window this change listener fires with
                    // a null value for deviceOffsetP.
                    if (deviceOffsetP == null)
                        return true;

                    // Make sure that the robot's width has already been set.
                    StartParameterInfo widthInfo = startParameters.get(StartParameter.ROBOT_WIDTH);

                    StartParameterInfo deviceOffsetInfo = startParameters.get(StartParameter.DEVICE_OFFSET_FROM_ROBOT_CENTER);
                    deviceOffsetInfo.setParameterValue(deviceOffsetP);
                    boolean deviceOffsetValid = (widthInfo != null && widthInfo.getValidity() &&
                            Math.abs(deviceOffsetInfo.getParameterValue()) < (widthInfo.getParameterValue() / 2));
                    deviceOffsetInfo.setValidity(deviceOffsetValid);
                    return deviceOffsetValid;
                },
                "The left/right distance from device center to robot center must be less than 1/2 the width of the robot"));

        validateStartParameter(pSimulatorController.device_offset_from_robot_center, deviceOffsetListener);

        //**TODO There must be limits on the approach position - neither
        // too close [nor too far left or right (camera field of view?)]
        // nor too far from the backdrop (must be in the second row of tiles).
        // Position by drag-and-drop of marker? Upper left? center?

        //## Normally you would want to position the robot opposite the
        // target AprilTag but you can use the values below to test different
        // positions.

        //  ROBOT_POSITION_AT_BACKDROP_X



        //  ROBOT_POSITION_AT_BACKDROP_Y

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






