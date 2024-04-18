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

public class StartParameterValidation {
    public static final double MIN_ROBOT_BODY_DIMENSION = 12.0;
    public static final double MAX_ROBOT_BODY_DIMENSION = 18.0;
    public static final double ROBOT_POSITION_AT_BACKDROP_X_MIN = 175.0 / FieldFXCenterStageBackdropLG.PX_PER_INCH;
    public static final double ROBOT_POSITION_AT_BACKDROP_X_MAX = 425.0 / FieldFXCenterStageBackdropLG.PX_PER_INCH;
    public static final double ROBOT_POSITION_AT_BACKDROP_Y_MIN = 225.0 / FieldFXCenterStageBackdropLG.PX_PER_INCH;
    public static final double ROBOT_POSITION_AT_BACKDROP_Y_MAX = 325.0 / FieldFXCenterStageBackdropLG.PX_PER_INCH;

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
        startParameters.put(StartParameter.ROBOT_WIDTH,
                new StartParameterInfo(pSimulatorController.robot_width.getText(), false)); // set default
        PredicateChangeListener widthListener = (new PredicateChangeListener(
                widthP -> {
                    // If the user doesn't enter a value for the width the animation runs
                    // but when the user closes the application window this change listener
                    // fires with a null value for widthP.
                    if (widthP == null)
                        return true;

                    if (widthP < MIN_ROBOT_BODY_DIMENSION || widthP > MAX_ROBOT_BODY_DIMENSION)
                        return false;

                    StartParameterInfo widthInfo = startParameters.get(StartParameter.ROBOT_WIDTH);
                    widthInfo.setParameterValue(widthP);
                    widthInfo.setValidity(true);
                    return true;
                },
                "The width of the robot must be between " + MIN_ROBOT_BODY_DIMENSION + " and " + MAX_ROBOT_BODY_DIMENSION));

        validateStartParameter(pSimulatorController.robot_width, widthListener);

        // Robot height.
        startParameters.put(StartParameter.ROBOT_HEIGHT,
                new StartParameterInfo(pSimulatorController.robot_height.getText(), false)); // set default
        PredicateChangeListener heightListener = (new PredicateChangeListener(
                heightP -> {
                    // If the user doesn't enter a value for the height the animation runs
                    // but when the user closes the application window this change listener
                    // fires with a null value for heightP.
                    if (heightP == null)
                        return true;

                    if (heightP < MIN_ROBOT_BODY_DIMENSION || heightP > MAX_ROBOT_BODY_DIMENSION)
                        return false;

                    StartParameterInfo heightInfo = startParameters.get(StartParameter.ROBOT_HEIGHT);
                    heightInfo.setParameterValue(heightP);
                    heightInfo.setValidity(true);
                    return true;
                },
                "The height of the robot must be between " + MIN_ROBOT_BODY_DIMENSION + " and " + MAX_ROBOT_BODY_DIMENSION));

        validateStartParameter(pSimulatorController.robot_height, heightListener);

        // CAMERA_CENTER_FROM_ROBOT_CENTER_ID
        // constraint - camera top or bottom edge may be no more than half the height of the robot from the center
        // the edge depends on the sign of the parameter.
        startParameters.put(StartParameter.CAMERA_CENTER_FROM_ROBOT_CENTER,
                new StartParameterInfo(pSimulatorController.camera_center_from_robot_center.getText(), true)); // set default
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
                    if (heightInfo == null || !heightInfo.getValidity() ||
                            Math.abs(cameraCenterInfo.getParameterValue()) > (heightInfo.getParameterValue() / 2))
                        return false;

                    cameraCenterInfo.setParameterValue(cameraCenterP);
                    cameraCenterInfo.setValidity(true);
                    return true;
                },
                "The fore/aft distance from camera center to robot center must be less than 1/2 the height of the robot"));

        validateStartParameter(pSimulatorController.camera_center_from_robot_center, cameraCenterListener);


        // CAMERA_OFFSET_FROM_ROBOT_CENTER_ID
        // constraint - camera left or right edge may be no more than half the width of the robot from the center
        // the edge depends on the sign of the parameter.
        startParameters.put(StartParameter.CAMERA_OFFSET_FROM_ROBOT_CENTER,
                new StartParameterInfo(pSimulatorController.camera_offset_from_robot_center.getText(), true)); // set default
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
                    if (widthInfo == null || !widthInfo.getValidity() ||
                            Math.abs(cameraOffsetInfo.getParameterValue()) > (widthInfo.getParameterValue() / 2))
                        return false;

                    cameraOffsetInfo.setParameterValue(cameraOffsetP);
                    cameraOffsetInfo.setValidity(true);
                    return true;
                },
                "The left/right distance from camera center to robot center must be less than 1/2 the width of the robot"));

        validateStartParameter(pSimulatorController.camera_offset_from_robot_center, cameraOffsetListener);

        // DEVICE_CENTER_FROM_ROBOT_CENTER_ID
        // constraint - device top or bottom edge may be no more than half the height of the robot from the center
        // the edge depends on the sign of the parameter.
        startParameters.put(StartParameter.DEVICE_CENTER_FROM_ROBOT_CENTER,
                new StartParameterInfo(pSimulatorController.device_center_from_robot_center.getText(), true)); // set default
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
                    if (heightInfo == null || !heightInfo.getValidity() ||
                            Math.abs(deviceCenterInfo.getParameterValue()) > (heightInfo.getParameterValue() / 2))
                        return false;

                    deviceCenterInfo.setParameterValue(deviceCenterP);
                    deviceCenterInfo.setValidity(true);
                    return true;
                },
                "The fore/aft distance from device center to robot center must be less than 1/2 the height of the robot"));

        validateStartParameter(pSimulatorController.device_center_from_robot_center, deviceCenterListener);

        // DEVICE_OFFSET_FROM_ROBOT_CENTER_ID
        // constraint - device left or right edge may be no more than half the width of the robot from the center
        // the edge depends on the sign of the parameter.
        startParameters.put(StartParameter.DEVICE_OFFSET_FROM_ROBOT_CENTER,
                new StartParameterInfo(pSimulatorController.device_offset_from_robot_center.getText(), true)); // set default
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
                    if (widthInfo == null || !widthInfo.getValidity() ||
                            Math.abs(deviceOffsetInfo.getParameterValue()) > (widthInfo.getParameterValue() / 2))
                        return false;

                    deviceOffsetInfo.setParameterValue(deviceOffsetP);
                    deviceOffsetInfo.setValidity(true);
                    return true;
                },
                "The left/right distance from device center to robot center must be less than 1/2 the width of the robot"));

        validateStartParameter(pSimulatorController.device_offset_from_robot_center, deviceOffsetListener);

        // ROBOT_POSITION_AT_BACKDROP_X
        // constraints: center x no less than 175 PX, no greater than 425 PX
        startParameters.put(StartParameter.ROBOT_POSITION_AT_BACKDROP_X,
                new StartParameterInfo(pSimulatorController.robot_position_at_backdrop_x.getText(), true)); // set default
        PredicateChangeListener backdropXListener = (new PredicateChangeListener(
                backdropXP -> {
                    // If the user doesn't enter a value the animation runs but when the
                    // user closes the application window this change listener fires with
                    // a null value for backdropXP.
                    if (backdropXP == null)
                        return true;

                    // Make sure that the robot's x position at the backdrop has already been set.
                    StartParameterInfo backdropXInfo = startParameters.get(StartParameter.ROBOT_POSITION_AT_BACKDROP_X);
                    if (backdropXInfo.getParameterValue() < ROBOT_POSITION_AT_BACKDROP_X_MIN ||
                            backdropXInfo.getParameterValue() > ROBOT_POSITION_AT_BACKDROP_X_MAX)
                        return false;

                    backdropXInfo.setParameterValue(backdropXP);
                    backdropXInfo.setValidity(true);
                    return true;
                },
                "The x position of the robot at the backstop is out of range"));

        validateStartParameter(pSimulatorController.robot_position_at_backdrop_x, backdropXListener);

        // ROBOT_POSITION_AT_BACKDROP_Y
        // constraints: center y no less than 225 PX, no greater than 325 PX.
        startParameters.put(StartParameter.ROBOT_POSITION_AT_BACKDROP_Y,
                new StartParameterInfo(pSimulatorController.robot_position_at_backdrop_y.getText(), true)); // set default
        PredicateChangeListener backdropYListener = (new PredicateChangeListener(
                backdropYP -> {
                    // If the user doesn't enter a value the animation runs but when the
                    // user closes the application window this change listener fires with
                    // a null value for backdropYP.
                    if (backdropYP == null)
                        return true;

                    // Make sure that the robot's y position at the backdrop has already been set.
                    StartParameterInfo backdropYInfo = startParameters.get(StartParameter.ROBOT_POSITION_AT_BACKDROP_Y);
                    if (backdropYInfo.getParameterValue() < ROBOT_POSITION_AT_BACKDROP_Y_MIN &&
                            backdropYInfo.getParameterValue() > ROBOT_POSITION_AT_BACKDROP_Y_MAX)
                        return false;

                    backdropYInfo.setParameterValue(backdropYP);
                    backdropYInfo.setValidity(true);
                    return true;
                },
                "The y position of the robot at the backstop is out of range"));

        validateStartParameter(pSimulatorController.robot_position_at_backdrop_y, backdropYListener);
    }

    // This method must only be called after all parameters have been validated.
    public double getStartParameter(StartParameter pSelectedParameter) {
        StartParameterInfo info = startParameters.get(pSelectedParameter);
        if (!info.valid)
            throw new IllegalStateException("Requested start parameter " + pSelectedParameter + " is not valid");
        return info.parameterValue;
    }

    public boolean allStartParametersValid() {
        Optional<StartParameterInfo> invalidEntry = startParameters.values().stream()
                .filter(e -> !e.getValidity())
                .findAny();
        return invalidEntry.isEmpty(); // returns true if there are no invalid entries
    }

    private void validateStartParameter(TextField pTextField,
                                        PredicateChangeListener pPredicateChangeListener) {
        double initialValue = Double.parseDouble(pTextField.getText());
        ObjectProperty<Double> valueProperty = new SimpleObjectProperty<>(initialValue);
        TextFormatter<Double> textFormatter = new TextFormatter<>(new DoubleStringConverter());
        textFormatter.valueProperty().bindBidirectional(valueProperty);
        pTextField.setTextFormatter(textFormatter);
        valueProperty.addListener(pPredicateChangeListener);
    }

    public static class StartParameterInfo {
        private double parameterValue;
        private boolean valid;

        public StartParameterInfo(String pParameterValue, boolean pValid) {
            parameterValue = Double.parseDouble(pParameterValue);
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
            }
        }
    }

}






