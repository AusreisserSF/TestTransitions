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
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.firstinspires.ftc.ftcdevcommon.platform.intellij.WorkingDirectory;
import org.xml.sax.SAXException;
import sample.auto.fx.FieldFXCenterStageBackdropLG;
import sample.auto.fx.RobotFXCenterStageLG;
import sample.auto.fx.CenterStageControllerLG;
import sample.auto.xml.StartParameters;
import sample.auto.xml.StartParametersXML;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

//**TODO Migrate all classes and fxml (some copy, some move) from this
// test project, TestTransitions, into its own project.
public class CenterStageBackdrop extends Application {

    private CenterStageControllerLG controller;
    private StartParameterValidation startParameterValidation;
    private RobotFXCenterStageLG centerStageRobot;

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

        String allianceString = allianceSelection(pStage, rootScene);
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

        // Show the play button now but when it is pressed then validate all of
        // the start parameters before allowing the animation to proceed.
        Button playPauseButton = new Button("Play");
        playPauseButton.setFont(Font.font("Verdana", FontWeight.BOLD, 12));

        // Position the button on the opposite side of the field from
        // the selected alliance.
        playPauseButton.setLayoutY(FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 3 - 50);
        if (alliance == RobotConstants.Alliance.BLUE)
            playPauseButton.setLayoutX((FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 3) - FieldFXCenterStageBackdropLG.FIELD_OUTSIDE_BORDER_SIZE - 60);
        else
            playPauseButton.setLayoutX(FieldFXCenterStageBackdropLG.FIELD_OUTSIDE_BORDER_SIZE + 10);

        field.getChildren().add(playPauseButton);
        AtomicReference<EventHandler<ActionEvent>> event = new AtomicReference<>();
        event.set((e) -> {
            if (!startParameterValidation.allStartParametersValid()) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Invalid request to Play the animation");
                errorAlert.setContentText("Not all start parameters have been set correctly");
                errorAlert.showAndWait();
                return;
            }

            //**TODO Is there a way to use the camera's FOV to validate the approach
            // position. Yes, if you make room on the start parameters screen and set
            // a parameter for it. Or just hardcode 78 degrees and put up an alert
            // after you get the camera to target angle in DeviceToTargetAnimation.

            playPauseButton.removeEventHandler(ActionEvent.ACTION, event.get());

            // Freeze the start parameters after the Play button has been hit.
            controller.start_parameters.setDisable(true);

            //## Here it looks like the position of the robot is that of the
            // upper left corner; Paths use the center point.

            // Place the robot on the field with the dimensions entered by the user.
            double robotWidthIn = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_WIDTH);
            double robotHeightIn = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_HEIGHT);
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

            // Collect start parameters.
            double cameraCenterFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.CAMERA_CENTER_FROM_ROBOT_CENTER);
            double cameraOffsetFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.CAMERA_OFFSET_FROM_ROBOT_CENTER);
            double deviceCenterFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.DEVICE_CENTER_FROM_ROBOT_CENTER);
            double deviceOffsetFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.DEVICE_OFFSET_FROM_ROBOT_CENTER);
            double robotPositionAtBackdropX = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_POSITION_AT_BACKDROP_X) * FieldFXCenterStageBackdropLG.PX_PER_INCH;
            double robotPositionAtBackdropY = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_POSITION_AT_BACKDROP_Y) * FieldFXCenterStageBackdropLG.PX_PER_INCH;
            Integer targetAprilTag = controller.april_tag_spinner.getValue();
            String radioButtonText = ((RadioButton) controller.approach_toggle.getSelectedToggle()).getText();

            centerStageRobot = new RobotFXCenterStageLG(robotWidthIn, robotHeightIn, Color.GREEN,
                    cameraCenterFromRobotCenter, cameraOffsetFromRobotCenter, deviceCenterFromRobotCenter, deviceOffsetFromRobotCenter,
                    startingPosition, startingRotation);

            Group robot = centerStageRobot.getRobot();
            field.getChildren().add(robot);

            System.out.println("Alliance " + alliance);
            System.out.println("Camera center from robot center " + cameraCenterFromRobotCenter);
            System.out.println("Camera offset from robot center " + cameraOffsetFromRobotCenter);
            System.out.println("Device center from robot center " + deviceCenterFromRobotCenter);
            System.out.println("Device offset from robot center " + deviceOffsetFromRobotCenter);
            System.out.println("Position at backdrop " + robotPositionAtBackdropX + ", y " + robotPositionAtBackdropY);
            System.out.println("AprilTag Id " + targetAprilTag);
            System.out.println("Approach " + radioButtonText);

            // Animate the movements of the robot from its starting position
            // to its final position in which the delivery device is aligned with
            // the target.
            DeviceToTargetAnimation animation = new DeviceToTargetAnimation(controller, field, centerStageRobot, startParameterValidation);
            animation.runDeviceToTargetAnimation(alliance, playPauseButton);
        });

        playPauseButton.setOnAction(event.get());
    }

    //**TODO What I really want is a RadioButtonDialog, which doesn't exist.
    // But it looks like you may be able make a custom Dialog with
    // RadioButton(s)/Toggle group inside it. But this will take some work.
    private String allianceSelection(Stage pStage, Scene pRootScene) {
        /*
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("allianceToggle.fxml"));
        AnchorPane root = fxmlLoader.load();
        AllianceToggleController controller = fxmlLoader.getController();
        */

        Button okButton = new Button("OK");

        // Items for the dialog.
        String[] alliances = {"BLUE", "RED"};
        ChoiceDialog<String> allianceDialog = new ChoiceDialog<>(alliances[0], alliances);

        allianceDialog.setHeaderText("Select alliance and confirm, fill in start parameters, hit Play");
        allianceDialog.setContentText("Please select your alliance");
        allianceDialog.showAndWait();

        // get the selected item
        String allianceSelection = allianceDialog.getSelectedItem();

        // action event
        EventHandler<ActionEvent> event = e -> allianceDialog.show();

        // When the OK button is pressed.
        okButton.setOnAction(event);

        // Create a pane for the button.
        TilePane dialogTilePane = new TilePane();
        dialogTilePane.getChildren().add(okButton);

        // Create a scene for the dialog and show it.
        Scene dialogScene = new Scene(dialogTilePane, 200, 200);
        pStage.setScene(dialogScene);

        // Restore the main scene.
        pStage.setScene(pRootScene);

        return allianceSelection;
    }

    private void overrideStartParameters(StartParameters pStartParameters) {
        /*
           @FXML
    public TextField robot_width;
    @FXML
    public TextField robot_height;
    @FXML
    public TextField camera_center_from_robot_center;
    @FXML
    public TextField camera_offset_from_robot_center;
    @FXML
    public TextField device_center_from_robot_center;
    @FXML
    public TextField device_offset_from_robot_center;
    @FXML
    public TextField robot_position_at_backdrop_x;
    @FXML
    public TextField robot_position_at_backdrop_y;
         */

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