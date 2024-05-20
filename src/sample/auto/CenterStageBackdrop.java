package sample.auto;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
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

//**TODO Documentation
// This is really about positioning the robot with respect to
// a target whose angle and distance from a camera mounted on a robot
// can be determined - such as an AprilTag or, as in this simulation,
// the three AprilTags on the backdrop of the CenterStage game.
// Assumptions/restrictions ...
// The robot is square or rectangular.
// The face of the camera is parallel to the side of the robot that
// faces the target AprilTag at the point the camera gets the angle
// and distance to the AprilTag (simulated here).
// The same side of the robot is assumed to be parallel to a line
// drawn through the center of the target AprilTag, i.e. there is
// no "yaw".

//**TODO Migrate all classes and fxml (some copy, some move) from this
// test project, TestTransitions, into its own project.

//## Includes a turret simulation, i.e. one in which the body of the
// robot is parallel to the plane of the target but the delivery
// device itself turns towards the target, not the robot itself.
public class CenterStageBackdrop extends Application {

    private CenterStageControllerLG controller;
    private StartParameterValidation startParameterValidation;
    private Button playButton;
    private Button saveButton;

    //## NOTE: I mistakenly investigated "drag-and-drop" but in JavaFX this
    // has to do with dragging and dropping content. All I need to do is drag
    // and release Nodes.
    @Override
    public void start(final Stage pStage) throws IOException, ParserConfigurationException, SAXException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/sample/auto/fx/CenterStageLG.fxml")); // absolute path under src
        BorderPane root = fxmlLoader.load();
        controller = fxmlLoader.getController();
        Pane field = controller.field;

        pStage.setTitle("FTC Center Stage Backdrop and AprilTags");
        Scene rootScene = new Scene(root);
        pStage.setScene(rootScene);
        pStage.show(); // show the empty field

        String allianceString = allianceSelection();
        RobotConstants.Alliance alliance = RobotConstants.Alliance.valueOf(allianceString);
        System.out.println("Alliance " + alliance);

        // Draw the alliance-specific view of the field.
        new FieldFXCenterStageBackdropLG(alliance, field);

        // Show the alliance id in its color.
        controller.alliance.setText(allianceString);
        controller.alliance.setFont(Font.font("System", FontWeight.BOLD, 14));
        Color allianceColor = (alliance == RobotConstants.Alliance.BLUE) ? Color.BLUE : Color.RED;
        controller.alliance.setTextFill(allianceColor); // or jewelsea setStyle("-fx-text-inner-color: red;");

        // Read start parameters from an XML file.
        StartParametersXML startParametersXML = new StartParametersXML(WorkingDirectory.getWorkingDirectory() + RobotConstants.XML_DIR);
        StartParameters startParameters = startParametersXML.getStartParameters();

        // Set up the start parameters with the values specified in the fxml.
        startParameterValidation = new StartParameterValidation(controller, startParametersXML);

        // Override the default start parameters with those from the XML file.
        overrideStartParameters(startParameters);
        startParameterValidation.enableXMLUpdate();

        SpinnerValueFactory.IntegerSpinnerValueFactory spinnerValueFactory;
        if (alliance == RobotConstants.Alliance.BLUE)
            spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 3);
        else // RED
            spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(4, 6);

        controller.april_tag_spinner.setValueFactory(spinnerValueFactory);

        // Show the Preview button now; when it is pressed validate all of the start
        // parameters before switching to the Play/Pause button.
        Button previewButton = controller.preview_button;
        previewButton.setVisible(true);
        previewButton.toFront();

        // Actions to take when the Preview button has been hit.
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
            freezeStartParameters();
            field.getChildren().remove(previewButton);

            // Show the Play/Pause and "Save preview changes" buttons for the actual animation.
            if (alliance == RobotConstants.Alliance.BLUE) {
                playButton = controller.play_button_blue;
                saveButton = controller.save_preview_changes_blue;
                controller.play_button_red.setDisable(true);
                controller.play_button_red.setDisable(true);
            }
            else {
                playButton = controller.play_button_red;
                saveButton = controller.save_preview_changes_red;
                controller.play_button_blue.setDisable(true);
                controller.play_button_blue.setDisable(true);
            }

            playButton.setVisible(true);
            playButton.toFront(); //!! absolutely necessary or the button will not fire on mouse clicks
            saveButton.setVisible(true);
            saveButton.toFront(); //!! absolutely necessary or the button will not fire on mouse clicks

            // At this point the start parameters are frozen and the Play button is showing.
            // Get ready to show the preview robot.

            // Mark a portion of the field as the approach zone, i.e. the area in which
            // the robot can stop in front of the backdrop. The outside boundaries of the
            // robot must be inside the approach zone.
            double robotWidthIn = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_WIDTH);
            double robotHeightIn = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_HEIGHT);
            double approachZoneX = StartParameterValidation.ROBOT_POSITION_AT_BACKDROP_X_MIN * FieldFXCenterStageBackdropLG.PX_PER_INCH -
                    (robotWidthIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2;
            double approachZoneY = StartParameterValidation.ROBOT_POSITION_AT_BACKDROP_Y_MIN * FieldFXCenterStageBackdropLG.PX_PER_INCH -
                    (robotHeightIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2;
            double approachZoneWidth = ((StartParameterValidation.ROBOT_POSITION_AT_BACKDROP_X_MAX * FieldFXCenterStageBackdropLG.PX_PER_INCH +
                    (robotWidthIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2)) - approachZoneX;
            double approachZoneHeight = ((StartParameterValidation.ROBOT_POSITION_AT_BACKDROP_Y_MAX * FieldFXCenterStageBackdropLG.PX_PER_INCH +
                    (robotHeightIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2)) - approachZoneY;
            Rectangle approachZone = new Rectangle(approachZoneX, approachZoneY, approachZoneWidth, approachZoneHeight);
            approachZone.setId("approachZone");
            approachZone.setFill(Color.TRANSPARENT);
            approachZone.setStroke(Color.BLACK);
            approachZone.getStrokeDashArray().addAll(5.0);
            approachZone.setStrokeWidth(2.0);
            field.getChildren().add(approachZone);

            // Collect the default start parameters.
            double cameraCenterFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.CAMERA_CENTER_FROM_ROBOT_CENTER);
            double cameraOffsetFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.CAMERA_OFFSET_FROM_ROBOT_CENTER);
            double cameraFieldOfView = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.CAMERA_FIELD_OF_VIEW);
            double deviceCenterFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.DEVICE_CENTER_FROM_ROBOT_CENTER);
            double deviceOffsetFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.DEVICE_OFFSET_FROM_ROBOT_CENTER);
            double robotPositionAtBackdropX = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_POSITION_AT_BACKDROP_X) * FieldFXCenterStageBackdropLG.PX_PER_INCH;
            double robotPositionAtBackdropY = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_POSITION_AT_BACKDROP_Y) * FieldFXCenterStageBackdropLG.PX_PER_INCH;
            Integer targetAprilTag = controller.april_tag_spinner.getValue();
            String radioButtonText = ((RadioButton) controller.approach_toggle.getSelectedToggle()).getText();

            // Positioning is determined by the upper left corner of the robot.
            RobotFXCenterStageLG previewRobot = new RobotFXCenterStageLG(RobotFXCenterStageLG.PREVIEW_ROBOT_ID,
                    robotWidthIn, robotHeightIn, Color.GREEN,
                    cameraCenterFromRobotCenter, cameraOffsetFromRobotCenter, cameraFieldOfView,
                    deviceCenterFromRobotCenter, deviceOffsetFromRobotCenter,
                    new Point2D(robotPositionAtBackdropX - ((robotWidthIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2),
                            robotPositionAtBackdropY - ((robotHeightIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2)),
                    0.0);

            // Show the draggable preview robot and its internally draggable camera and device.
            Rectangle aprilTag = (Rectangle) field.lookup("#" + FieldFXCenterStageBackdropLG.APRIL_TAG_ID + targetAprilTag);
            new PreviewDragAndRelease(controller, field, approachZone, previewRobot, aprilTag);

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

            // The Preview button is showing: these fields may not be changed.
            System.out.println("Read-only start parameters");
            System.out.println("Robot width inches " + robotWidthIn);
            System.out.println("Robot height inches " + robotHeightIn);
            System.out.println("Camera field of view " + cameraFieldOfView);
            System.out.println("AprilTag Id " + targetAprilTag);
            System.out.println("Approach " + radioButtonText);

            // Animate the movements of the robot from its starting position to
            // its final position in which the delivery device is aligned with
            // the target. At this point the preview robot and camera field-of-
            // view display are on the screen and the Play button is visible.
            new DeviceToTargetAnimation(alliance, controller, field, previewRobot,
                    startParameterValidation, startingPosition, startingRotation,
                    playButton, saveButton, startParametersXML);
        });

        previewButton.setOnAction(event.get());
    }

    private String allianceSelection() {
        // Create the custom dialog.
        VBox allianceButtons = new VBox(5); // spacing
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

    // Override the default start parameters with those from the XML file -
    // hut only if the XML values are different from the defaults.
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

        if (!pStartParameters.cameraFieldOfView.equals(controller.camera_field_of_view.getText())) {
            controller.camera_field_of_view.setText(pStartParameters.cameraFieldOfView);
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
            controller.robot_position_at_backdrop_y.setText(pStartParameters.robotPositionAtBackdropY);
        }
    }

    // Set uneditable all of the start parameters that we want to update during
    // the drag-and-release of the preview robot, the camera, and the device.
    // Disable all others.
    private void freezeStartParameters() {
        controller.robot_width.setDisable(true);
        controller.robot_height.setDisable(true);
        controller.camera_center_from_robot_center.setEditable(false);
        controller.camera_offset_from_robot_center.setEditable(false);
        controller.camera_field_of_view.setEditable(false);
        controller.device_center_from_robot_center.setEditable(false);
        controller.device_offset_from_robot_center.setEditable(false);
        controller.robot_position_at_backdrop_x.setEditable(false);
        controller.robot_position_at_backdrop_y.setEditable(false);
        controller.april_tag_spinner.setDisable(true);
        controller.approach_toggle.getToggles().forEach(toggle -> {
            Node node = (Node) toggle ;
            node.setDisable(true);
        });
    }

}