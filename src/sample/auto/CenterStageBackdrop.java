package sample.auto;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.firstinspires.ftc.ftcdevcommon.platform.intellij.WorkingDirectory;
import org.xml.sax.SAXException;
import sample.auto.fx.CenterStageControllerLG;
import sample.auto.fx.FieldFXCenterStageBackdropLG;
import sample.auto.fx.RobotFXCenterStageLG;
import sample.auto.xml.StartParameters;
import sample.auto.xml.StartParametersXML;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

//**TODO This is really about positioning the robot with respect to
// and AprilTag; the backdrop is just an example.

//**TODO Migrate all classes and fxml (some copy, some move) from this
// test project, TestTransitions, into its own project.

//**TODO Consider a turret simulation, i.e. one in which the body of
// the robot is parallel to the plane of the target but the delivery
// device itself turns towards the target, not the robot itself.
public class CenterStageBackdrop extends Application {

    private CenterStageControllerLG controller;
    private StartParameterValidation startParameterValidation;

    //## NOTE: I mistakenly investigated "drag-and-drop" but in JavaFX this
    // has to do with dragging and dropping content. All I need to do is drag
    // Nodes. See the sample in the Drag class.
    @Override
    public void start(final Stage pStage) throws IOException, ParserConfigurationException, SAXException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/sample/auto/fx/centerStageLG.fxml")); // absolute path under src
        BorderPane root = fxmlLoader.load();
        controller = fxmlLoader.getController();
        Pane field = controller.field;

        pStage.setTitle("FTC Center Stage Backdrop and AprilTags");
        Scene rootScene = new Scene(root);
        pStage.setScene(rootScene);
        pStage.show(); // show the empty field

        String allianceString = allianceSelection();
        RobotConstants.Alliance alliance = RobotConstants.Alliance.valueOf(allianceString);

        // Draw the alliance-specific view of the field.
        new FieldFXCenterStageBackdropLG(alliance, field);

        // Show the alliance id in its color.
        controller.alliance.setText(allianceString);
        controller.alliance.setFont(Font.font("System", FontWeight.BOLD, 14));
        Color allianceColor = (alliance == RobotConstants.Alliance.BLUE) ? Color.BLUE : Color.RED;
        controller.alliance.setTextFill(allianceColor); // or jewelsea setStyle("-fx-text-inner-color: red;");

        // Set up the start parameters with the value specified in the fxml.
        startParameterValidation = new StartParameterValidation(controller);

        // Read start parameters from an XML file and for each value that is different
        // from the current fxml default trigger the change listener already set up in
        // StartParameterValidation.
        StartParametersXML startParametersXML = new StartParametersXML(WorkingDirectory.getWorkingDirectory() + RobotConstants.XML_DIR);
        StartParameters startParameters = startParametersXML.getStartParameters();
        overrideStartParameters(startParameters);

        SpinnerValueFactory.IntegerSpinnerValueFactory spinnerValueFactory;
        if (alliance == RobotConstants.Alliance.BLUE)
            spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 3);
        else // RED
            spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(4, 6);

        controller.april_tag_spinner.setValueFactory(spinnerValueFactory);

        // Show the Preview button now; when it is pressed validate all of the start
        // parameters before switching to the Play/Pause button.
        Button previewButton = new Button("Preview");
        previewButton.setFont(Font.font("Verdana", FontWeight.BOLD, 12));

        // Place the button on the opposite side of the field from the selected
        // alliance.
        previewButton.setLayoutY(FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 3 - 50);
        if (alliance == RobotConstants.Alliance.BLUE)
            previewButton.setLayoutX((FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 3) - FieldFXCenterStageBackdropLG.FIELD_OUTSIDE_BORDER_SIZE - 75);
        else
            previewButton.setLayoutX(FieldFXCenterStageBackdropLG.FIELD_OUTSIDE_BORDER_SIZE + 10);

        field.getChildren().add(previewButton);
        AtomicReference<EventHandler<ActionEvent>> event = new AtomicReference<>();
        event.set((e) -> {
            if (!startParameterValidation.allStartParametersValid()) {
                // At least one parameter is still invalid; show the user all invalid parameters.
                List<StartParameterValidation.StartParameter> invalidParameters = startParameterValidation.getInvalidStartParameters();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Invalid request to position the robot for animation");
                StringBuilder collectedErrors = new StringBuilder("One or more start parameters has been set correctly:\n");
                invalidParameters.forEach(p -> {
                    collectedErrors.append(p);
                    collectedErrors.append('\n');
                });
                errorAlert.setContentText(collectedErrors.toString());
                errorAlert.showAndWait();
                return;
            }

            // Freeze the start parameters after the Preview button has been hit
            // and remove the Preview button.
            controller.start_parameters.setDisable(true);
            field.getChildren().remove(previewButton);

            // Show a Play/Pause button for the actual animation.
            Button animationButton = new Button("Play");
            animationButton.setFont(Font.font("Verdana", FontWeight.BOLD, 12));

            // Place the button on the opposite side of the field from the selected
            // alliance.
            animationButton.setLayoutY(FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 3 - 50);
            if (alliance == RobotConstants.Alliance.BLUE)
                animationButton.setLayoutX((FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 3) - FieldFXCenterStageBackdropLG.FIELD_OUTSIDE_BORDER_SIZE - 60);
            else
                animationButton.setLayoutX(FieldFXCenterStageBackdropLG.FIELD_OUTSIDE_BORDER_SIZE + 10);

            field.getChildren().add(animationButton);

            // Place the robot on the field with the dimensions entered by the user.
            double robotWidthIn = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_WIDTH);
            double robotHeightIn = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_HEIGHT);

            //**TODO When the start parameters are frozen and the preview screen is showing,
            // allow the user to
            // [drag the camera and device on the preview representation of the robot
            // and to]
            // drag and release the preview robot and the associated camera field of view
            // lines to set the final position of the robot in front of the backdrop.

            // Mark a portion of the field as the positioning zone, i.e. the area in which
            // the robot can stop in front of the backdrop. The outside boundaries of the
            // robot must be inside the positioning zone.
            double positioningZoneX = StartParameterValidation.ROBOT_POSITION_AT_BACKDROP_X_MIN * FieldFXCenterStageBackdropLG.PX_PER_INCH -
                    (robotWidthIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2;
            double positioningZoneY = StartParameterValidation.ROBOT_POSITION_AT_BACKDROP_Y_MIN * FieldFXCenterStageBackdropLG.PX_PER_INCH -
                    (robotHeightIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2;
            double positioningZoneWidth = ((StartParameterValidation.ROBOT_POSITION_AT_BACKDROP_X_MAX * FieldFXCenterStageBackdropLG.PX_PER_INCH +
                    (robotWidthIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2)) - positioningZoneX;
            double positioningZoneHeight = ((StartParameterValidation.ROBOT_POSITION_AT_BACKDROP_Y_MAX * FieldFXCenterStageBackdropLG.PX_PER_INCH +
                    (robotHeightIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2)) - positioningZoneY;
            Rectangle positioningZone = new Rectangle(positioningZoneX, positioningZoneY, positioningZoneWidth, positioningZoneHeight);
            positioningZone.setId("positioningZone");
            positioningZone.setFill(Color.TRANSPARENT);
            positioningZone.setStroke(Color.BLACK);
            positioningZone.getStrokeDashArray().addAll(5.0);
            positioningZone.setStrokeWidth(2.0);
            field.getChildren().add(positioningZone);

            // Collect the start parameters.
            double cameraCenterFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.CAMERA_CENTER_FROM_ROBOT_CENTER);
            double cameraOffsetFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.CAMERA_OFFSET_FROM_ROBOT_CENTER);
            double deviceCenterFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.DEVICE_CENTER_FROM_ROBOT_CENTER);
            double deviceOffsetFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.DEVICE_OFFSET_FROM_ROBOT_CENTER);
            double robotPositionAtBackdropX = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_POSITION_AT_BACKDROP_X) * FieldFXCenterStageBackdropLG.PX_PER_INCH;
            double robotPositionAtBackdropY = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_POSITION_AT_BACKDROP_Y) * FieldFXCenterStageBackdropLG.PX_PER_INCH;
            Integer targetAprilTag = controller.april_tag_spinner.getValue();
            String radioButtonText = ((RadioButton) controller.approach_toggle.getSelectedToggle()).getText();

            // Positioning is determined byt he upper left corner of the robot.
            RobotFXCenterStageLG previewRobot = new RobotFXCenterStageLG(RobotFXCenterStageLG.PREVIEW_ROBOT_ID,
                    robotWidthIn, robotHeightIn, Color.GREEN,
                    cameraCenterFromRobotCenter, cameraOffsetFromRobotCenter, deviceCenterFromRobotCenter, deviceOffsetFromRobotCenter,
                    new Point2D(robotPositionAtBackdropX - ((robotWidthIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2),
                            robotPositionAtBackdropY - ((robotHeightIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2)),
                    0.0);

            // Show the draggable preview robot and camera field of view.
            Group robotP = previewRobot.getRobot();
            new PreviewDragAndRelease(field, robotP);

            // Set the starting position for the animation robot.
            Point2D startingPosition;
            double startingRotation;
            if (alliance == RobotConstants.Alliance.BLUE) {
                startingPosition = new Point2D(FieldFXCenterStageBackdropLG.PX_PER_INCH * 1.5,
                        FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 2 + FieldFXCenterStageBackdropLG.PX_PER_INCH * 1.5);
                startingRotation = 90.0;
            } else { // RED
                startingPosition = new Point2D(FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 3 - ((robotWidthIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) + FieldFXCenterStageBackdropLG.PX_PER_INCH * 1.5),
                        FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 2 + FieldFXCenterStageBackdropLG.PX_PER_INCH * 1.5);
                startingRotation = -90.0;
            }

            // Create the animation robot now but do not show yet.
            RobotFXCenterStageLG animationRobot = new RobotFXCenterStageLG(RobotFXCenterStageLG.ANIMATION_ROBOT_ID,
                    robotWidthIn, robotHeightIn, Color.GREEN,
                    cameraCenterFromRobotCenter, cameraOffsetFromRobotCenter, deviceCenterFromRobotCenter, deviceOffsetFromRobotCenter,
                    startingPosition, startingRotation);

            System.out.println("Alliance " + alliance);
            System.out.println("Camera center from robot center " + cameraCenterFromRobotCenter);
            System.out.println("Camera offset from robot center " + cameraOffsetFromRobotCenter);
            System.out.println("Device center from robot center " + deviceCenterFromRobotCenter);
            System.out.println("Device offset from robot center " + deviceOffsetFromRobotCenter);
            //**TODO Can only be shown/logged after preview robot drag/drop is complete.
            System.out.println("Position at backdrop " + robotPositionAtBackdropX + ", y " + robotPositionAtBackdropY);
            System.out.println("AprilTag Id " + targetAprilTag);
            System.out.println("Approach " + radioButtonText);

            // Animate the movements of the robot from its starting position to
            // its final position in which the delivery device is aligned with
            // the target. At this point the preview robot and camera field-of-
            // view display are on the screen and the Play button is visible.
            DeviceToTargetAnimation animation = new DeviceToTargetAnimation(controller, field, previewRobot, animationRobot, startParameterValidation);
            animation.runDeviceToTargetAnimation(alliance, animationButton);
        });

        previewButton.setOnAction(event.get());
    }

    //**TODO Fix spacing.
    private String allianceSelection() {
        /*
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("allianceToggle.fxml"));
        AnchorPane root = fxmlLoader.load();
        AllianceToggleController controller = fxmlLoader.getController();
        */

        // Create the custom dialog.
        VBox allianceButtons = new VBox();
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Alliance selection");
        dialog.setHeaderText("Select alliance and confirm, fill in start parameters, hit Play");

        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        // Create radiobuttons.
        RadioButton blueButton = new RadioButton("BLUE");
        blueButton.setSelected(true);
        RadioButton redButton = new RadioButton("RED");

        // Create a toggle group for the buttons.
        ToggleGroup allianceToggleGroup = new ToggleGroup();

        // Add radiobuttons to toggle group
        blueButton.setToggleGroup(allianceToggleGroup);
        redButton.setToggleGroup(allianceToggleGroup);

        allianceButtons.getChildren().addAll(blueButton, redButton);
        dialog.getDialogPane().setContent(allianceButtons);

        // Convert the result to a String when the OK button is clicked.
        dialog.setResultConverter(dialogButton -> {
            // Return the selected button or, on cancel, the default button.
            return ((RadioButton) allianceToggleGroup.getSelectedToggle()).getText();
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse("BLUE");

    }

    private void overrideStartParameters(StartParameters pStartParameters) {

        // Any change to a TextField will trigger the ChangeListener already
        // registered in StartParameterValidation.
        if (!pStartParameters.robotWidth.equals(controller.robot_width.getText())) {
            controller.robot_width.setText(pStartParameters.robotWidth);
        }

        if (!pStartParameters.robotHeight.equals(controller.robot_height.getText())) {
            controller.robot_height.setText(pStartParameters.robotHeight);
        }

        if (!pStartParameters.cameraCenterFromRobotCenter.equals(controller.camera_center_from_robot_center.getText())) {
            controller.camera_center_from_robot_center.setText(pStartParameters.cameraCenterFromRobotCenter);
        }

        if (!pStartParameters.cameraOffsetFromRobotCenter.equals(controller.camera_offset_from_robot_center.getText())) {
            controller.camera_offset_from_robot_center.setText(pStartParameters.cameraOffsetFromRobotCenter);
        }

        if (!pStartParameters.deviceCenterFromRobotCenter.equals(controller.device_center_from_robot_center.getText())) {
            controller.device_center_from_robot_center.setText(pStartParameters.deviceCenterFromRobotCenter);
        }

        if (!pStartParameters.deviceOffsetFromRobotCenter.equals(controller.device_offset_from_robot_center.getText())) {
            controller.device_offset_from_robot_center.setText(pStartParameters.deviceOffsetFromRobotCenter);
        }

        if (!pStartParameters.robotPositionAtBackdropX.equals(controller.robot_position_at_backdrop_x.getText())) {
            controller.robot_position_at_backdrop_x.setText(pStartParameters.robotPositionAtBackdropX);
        }

        if (!pStartParameters.robotPositionAtBackdropY.equals(controller.robot_position_at_backdrop_y.getText())) {
            controller.robot_position_at_backdrop_x.setText(pStartParameters.robotPositionAtBackdropY);
        }
    }

}