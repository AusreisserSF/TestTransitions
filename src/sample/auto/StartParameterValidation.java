package sample.auto;


// From https://www.pragmaticcoding.ca/javafx/textformatter1

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.util.converter.DoubleStringConverter;
import org.firstinspires.ftc.ftcdevcommon.AutonomousRobotException;
import sample.auto.fx.CenterStageControllerLG;
import sample.auto.fx.FieldFXCenterStageBackdropLG;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StartParameterValidation {

    private final String TAG = StartParameterValidation.class.getSimpleName();
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

    public StartParameterValidation(CenterStageControllerLG pCenterStageControllerLG) {

        // Create one listener for each start parameter that takes a range of double values.
        // Robot width.
        startParameters.put(StartParameter.ROBOT_WIDTH,
                new StartParameterInfo(pCenterStageControllerLG.robot_width.getText(), false)); // set default
        PredicateChangeListener widthListener = (new PredicateChangeListener(
                pCenterStageControllerLG.robot_width,
                widthP -> {
                    // If the user doesn't enter a value for the width the animation runs
                    // but when the user closes the application window this change listener
                    // fires with a null value for widthP.
                    if (widthP == null)
                        return true;

                    StartParameterInfo widthInfo = startParameters.get(StartParameter.ROBOT_WIDTH);
                    if (widthP < MIN_ROBOT_BODY_DIMENSION || widthP > MAX_ROBOT_BODY_DIMENSION) {
                        widthInfo.setInvalid();
                        return false;
                    }

                    widthInfo.setValid(widthP);
                    return true;
                },
                "The width of the robot must be between " + MIN_ROBOT_BODY_DIMENSION + " and " + MAX_ROBOT_BODY_DIMENSION));

        validateStartParameter(widthListener);

        // Robot height.
        startParameters.put(StartParameter.ROBOT_HEIGHT,
                new StartParameterInfo(pCenterStageControllerLG.robot_height.getText(), false)); // set default
        PredicateChangeListener heightListener = (new PredicateChangeListener(
                pCenterStageControllerLG.robot_height,
                heightP -> {
                    // If the user doesn't enter a value for the height the animation runs
                    // but when the user closes the application window this change listener
                    // fires with a null value for heightP.
                    if (heightP == null)
                        return true;

                    StartParameterInfo heightInfo = startParameters.get(StartParameter.ROBOT_HEIGHT);
                    if (heightP < MIN_ROBOT_BODY_DIMENSION || heightP > MAX_ROBOT_BODY_DIMENSION) {
                        heightInfo.setInvalid();
                        return false;
                    }

                    heightInfo.setValid(heightP);
                    return true;
                },
                "The height of the robot must be between " + MIN_ROBOT_BODY_DIMENSION + " and " + MAX_ROBOT_BODY_DIMENSION));

        validateStartParameter(heightListener);

        // CAMERA_CENTER_FROM_ROBOT_CENTER
        // constraint - camera top or bottom edge may be no more than half the height of the robot from the center
        // the edge depends on the sign of the parameter.
        startParameters.put(StartParameter.CAMERA_CENTER_FROM_ROBOT_CENTER,
                new StartParameterInfo(pCenterStageControllerLG.camera_center_from_robot_center.getText(), true)); // set default
        PredicateChangeListener cameraCenterListener = (new PredicateChangeListener(
                pCenterStageControllerLG.camera_center_from_robot_center,
                cameraCenterP -> {
                    // If the user doesn't enter a value the animation runs but when the
                    // user closes the application window this change listener fires with
                    // a null value for cameraCenterP.
                    if (cameraCenterP == null)
                        return true;

                    // Make sure that the robot's height has already been set.
                    StartParameterInfo heightInfo = startParameters.get(StartParameter.ROBOT_HEIGHT);
                    StartParameterInfo cameraCenterInfo = startParameters.get(StartParameter.CAMERA_CENTER_FROM_ROBOT_CENTER);
                    if (heightInfo == null || !heightInfo.getValidity() || cameraCenterP > (heightInfo.getParameterValue() / 2)) {
                        cameraCenterInfo.setInvalid();
                        return false;
                    }

                    cameraCenterInfo.setValid(cameraCenterP);
                    return true;
                },
                "The fore/aft distance from camera center to robot center must be less than 1/2 the height of the robot"));

        validateStartParameter(cameraCenterListener);


        // CAMERA_OFFSET_FROM_ROBOT_CENTER
        // constraint - camera left or right edge may be no more than half the width of the robot from the center
        // the edge depends on the sign of the parameter.
        startParameters.put(StartParameter.CAMERA_OFFSET_FROM_ROBOT_CENTER,
                new StartParameterInfo(pCenterStageControllerLG.camera_offset_from_robot_center.getText(), true)); // set default
        PredicateChangeListener cameraOffsetListener = (new PredicateChangeListener(
                pCenterStageControllerLG.camera_offset_from_robot_center,
                cameraOffsetP -> {
                    // If the user doesn't enter a value the animation runs but when the
                    // user closes the application window this change listener fires with
                    // a null value for cameraOffsetP.
                    if (cameraOffsetP == null)
                        return true;

                    // Make sure that the robot's width has already been set.
                    StartParameterInfo widthInfo = startParameters.get(StartParameter.ROBOT_WIDTH);
                    StartParameterInfo cameraOffsetInfo = startParameters.get(StartParameter.CAMERA_OFFSET_FROM_ROBOT_CENTER);
                    if (widthInfo == null || !widthInfo.getValidity() || cameraOffsetP > (widthInfo.getParameterValue() / 2)) {
                        cameraOffsetInfo.setInvalid();
                        return false;
                    }

                    cameraOffsetInfo.setValid(cameraOffsetP);
                    return true;
                },
                "The left/right distance from camera center to robot center must be less than 1/2 the width of the robot"));

        validateStartParameter(cameraOffsetListener);

        // DEVICE_CENTER_FROM_ROBOT_CENTER
        // constraint - device top or bottom edge may be no more than half the height of the robot from the center
        // the edge depends on the sign of the parameter.
        startParameters.put(StartParameter.DEVICE_CENTER_FROM_ROBOT_CENTER,
                new StartParameterInfo(pCenterStageControllerLG.device_center_from_robot_center.getText(), true)); // set default
        PredicateChangeListener deviceCenterListener = (new PredicateChangeListener(
                pCenterStageControllerLG.device_center_from_robot_center,
                deviceCenterP -> {
                    // If the user doesn't enter a value the animation runs but when the
                    // user closes the application window this change listener fires with
                    // a null value for deviceCenterP.
                    if (deviceCenterP == null)
                        return true;

                    // Make sure that the robot's height has already been set.
                    StartParameterInfo heightInfo = startParameters.get(StartParameter.ROBOT_HEIGHT);
                    StartParameterInfo deviceCenterInfo = startParameters.get(StartParameter.DEVICE_CENTER_FROM_ROBOT_CENTER);
                    if (heightInfo == null || !heightInfo.getValidity() || deviceCenterP > (heightInfo.getParameterValue() / 2)) {
                        deviceCenterInfo.setInvalid();
                        return false;
                    }

                    deviceCenterInfo.setValid(deviceCenterP);
                    return true;
                },
                "The fore/aft distance from device center to robot center must be less than 1/2 the height of the robot"));

        validateStartParameter(deviceCenterListener);

        // DEVICE_OFFSET_FROM_ROBOT_CENTER
        // constraint - device left or right edge may be no more than half the width of the robot from the center
        // the edge depends on the sign of the parameter.
        startParameters.put(StartParameter.DEVICE_OFFSET_FROM_ROBOT_CENTER,
                new StartParameterInfo(pCenterStageControllerLG.device_offset_from_robot_center.getText(), true)); // set default
        PredicateChangeListener deviceOffsetListener = (new PredicateChangeListener(
                pCenterStageControllerLG.device_offset_from_robot_center,
                deviceOffsetP -> {
                    // If the user doesn't enter a value the animation runs but when the
                    // user closes the application window this change listener fires with
                    // a null value for deviceOffsetP.
                    if (deviceOffsetP == null)
                        return true;

                    // Make sure that the robot's width has already been set.
                    StartParameterInfo widthInfo = startParameters.get(StartParameter.ROBOT_WIDTH);
                    StartParameterInfo deviceOffsetInfo = startParameters.get(StartParameter.DEVICE_OFFSET_FROM_ROBOT_CENTER);
                    if (widthInfo == null || !widthInfo.getValidity() || deviceOffsetP > (widthInfo.getParameterValue() / 2)) {
                        deviceOffsetInfo.setInvalid();
                        return false;
                    }

                    deviceOffsetInfo.setValid(deviceOffsetP);
                    return true;
                },
                "The left/right distance from device center to robot center must be less than 1/2 the width of the robot"));

        validateStartParameter(deviceOffsetListener);

        // ROBOT_POSITION_AT_BACKDROP_X
        // constraints: center x no less than 175 PX, no greater than 425 PX
        startParameters.put(StartParameter.ROBOT_POSITION_AT_BACKDROP_X,
                new StartParameterInfo(pCenterStageControllerLG.robot_position_at_backdrop_x.getText(), true)); // set default
        PredicateChangeListener backdropXListener = (new PredicateChangeListener(
                pCenterStageControllerLG.robot_position_at_backdrop_x,
                backdropXP -> {
                    // If the user doesn't enter a value the animation runs but when the
                    // user closes the application window this change listener fires with
                    // a null value for backdropXP.
                    if (backdropXP == null)
                        return true;

                    // Make sure that the robot's x position at the backdrop is in range.
                    StartParameterInfo backdropXInfo = startParameters.get(StartParameter.ROBOT_POSITION_AT_BACKDROP_X);
                    if (backdropXP < ROBOT_POSITION_AT_BACKDROP_X_MIN || backdropXP > ROBOT_POSITION_AT_BACKDROP_X_MAX) {
                        backdropXInfo.setInvalid();
                        return false;
                    }

                    backdropXInfo.setValid(backdropXP);
                    return true;
                },
                "The x position of the robot at the backstop is out of range"));

        validateStartParameter(backdropXListener);

        // ROBOT_POSITION_AT_BACKDROP_Y
        // constraints: center y no less than 225 PX, no greater than 325 PX.
        startParameters.put(StartParameter.ROBOT_POSITION_AT_BACKDROP_Y,
                new StartParameterInfo(pCenterStageControllerLG.robot_position_at_backdrop_y.getText(), true)); // set default
        PredicateChangeListener backdropYListener = (new PredicateChangeListener(
                pCenterStageControllerLG.robot_position_at_backdrop_y,
                backdropYP -> {
                    // If the user doesn't enter a value the animation runs but when the
                    // user closes the application window this change listener fires with
                    // a null value for backdropYP.
                    if (backdropYP == null)
                        return true;

                    // Make sure that the robot's y position at the backdrop is in range.
                    StartParameterInfo backdropYInfo = startParameters.get(StartParameter.ROBOT_POSITION_AT_BACKDROP_Y);
                    if (backdropYP < ROBOT_POSITION_AT_BACKDROP_Y_MIN || backdropYP > ROBOT_POSITION_AT_BACKDROP_Y_MAX) {
                        backdropYInfo.setInvalid();
                        return false;
                    }

                    backdropYInfo.setValid(backdropYP);
                    return true;
                },
                "The y position of the robot at the backstop is out of range"));

        validateStartParameter(backdropYListener);
    }

    // This method must only be called after all parameters have been validated.
    public double getStartParameter(StartParameter pSelectedParameter) {
        StartParameterInfo info = startParameters.get(pSelectedParameter);
        if (!info.valid)
            throw new AutonomousRobotException(TAG, "Requested start parameter " + pSelectedParameter + " is not valid");
        return info.parameterValue;
    }

    public boolean allStartParametersValid() {
        Optional<StartParameterInfo> invalidEntry = startParameters.values().stream()
                .filter(e -> !e.getValidity())
                .findAny();
        return invalidEntry.isEmpty(); // returns true if there are no invalid entries
    }

    public List<StartParameter> getInvalidStartParameters() {
        return startParameters.entrySet().stream()
                .filter(e -> !e.getValue().valid)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // Based on https://www.pragmaticcoding.ca/javafx/textformatter1
    private void validateStartParameter(PredicateChangeListener pPredicateChangeListener) {
        double initialValue = Double.parseDouble(pPredicateChangeListener.getTextField().getText());
        ObjectProperty<Double> valueProperty = new SimpleObjectProperty<>(initialValue);
        TextFormatter<Double> textFormatter = new TextFormatter<>(new DoubleStringConverter());
        textFormatter.valueProperty().bindBidirectional(valueProperty);
        pPredicateChangeListener.getTextField().setTextFormatter(textFormatter);
        valueProperty.addListener(pPredicateChangeListener);
    }

    public static class StartParameterInfo {
        private double parameterValue;
        private boolean valid;

        public StartParameterInfo(String pParameterValue, boolean pValid) {
            valid = pValid;
            try {
                parameterValue = Double.parseDouble(pParameterValue);
            } catch (NumberFormatException nex) {
                valid = false;
            }
        }

        public void setInvalid() {
            parameterValue = 0.0;
            valid = false;
        }

        public void setValid(double pValue) {
            parameterValue = pValue;
            valid = true;
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
        private final TextField textField;
        private final Predicate<Double> changePredicate;
        private final String errorMsg;

        PredicateChangeListener(TextField pTextField, Predicate<Double> pChangePredicate, String pErrorMsg) {
            textField = pTextField;
            changePredicate = pChangePredicate;
            errorMsg = pErrorMsg;
        }

        // Use a TextDialog to get a corrected value.
        // See https://code.makery.ch/blog/javafx-dialogs-official/
        @Override
        public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
            if (changePredicate.test(newValue))
                return; // valid input - nothing to do

            // Invalid input: pester the user until he enters valid input or cancels out,
            // which we'll catch later.
            boolean doneWithPestering = false;
            while (!doneWithPestering) {

                // TextInputDialog from https://code.makery.ch/blog/javafx-dialogs-official/
                TextInputDialog dialog = new TextInputDialog("0.0");
                dialog.setTitle("Error Correction Dialog");
                dialog.setHeaderText(errorMsg);
                dialog.setContentText("Please enter a new value:");
                Optional<String> result = dialog.showAndWait();
                if (result.isEmpty()) {
                    System.out.println("You cancelled the dialog; the last input remains invalid");
                    doneWithPestering = true;
                } else {
                    double correctedValue;
                    try {
                        correctedValue = Double.parseDouble(result.get());
                    } catch (NumberFormatException nex) {
                        System.out.println("You did not enter a valid double");
                        continue;
                    }

                    textField.setText(result.get());
                    System.out.println("You entered a corrected value of " + textField.getText());

                    doneWithPestering = changePredicate.test(correctedValue);
                }
            }
        }

        public TextField getTextField() {
            return textField;
        }
    }

}






