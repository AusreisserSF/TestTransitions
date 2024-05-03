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
import javafx.scene.shape.Line;
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
    private RobotFXCenterStageLG centerStageRobot;

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

        // Show the animation (Position/Play/Pause) button now but when Position
        // is pressed validate all of the start parameters before showing Play.
        Button animationButton = new Button("Position");
        animationButton.setFont(Font.font("Verdana", FontWeight.BOLD, 12));

        // Position the button on the opposite side of the field from the selected
        // alliance.
        animationButton.setLayoutY(FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 3 - 50);
        if (alliance == RobotConstants.Alliance.BLUE)
            animationButton.setLayoutX((FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 3) - FieldFXCenterStageBackdropLG.FIELD_OUTSIDE_BORDER_SIZE - 75);
        else
            animationButton.setLayoutX(FieldFXCenterStageBackdropLG.FIELD_OUTSIDE_BORDER_SIZE + 10);

        field.getChildren().add(animationButton);
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

            //**TODO Is there a way to use the camera's FOV to validate the approach
            // position. Yes, if you make room on the start parameters screen and set
            // a parameter for it. Or just hardcode 78 degrees and put up an alert
            // after you get the camera to target angle in DeviceToTargetAnimation.

            animationButton.removeEventHandler(ActionEvent.ACTION, event.get());

            // Freeze the start parameters after the Play button has been hit.
            controller.start_parameters.setDisable(true);

            //## Here it looks like the position of the robot is that of the
            // upper left corner; Paths use the center point.

            //**TODO For the Position screen you need the robot's dimensions but the
            // position is in front of the backdrop and the rotation is 0.
            //**TODO How to hand off control to the Play button?

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

            //**TODO Improvement: keep the fill-in start parameters but change the
            // animation button to include "Position" as its required first choice.
            // This button freezes the fill-in parameters (except for the two approach
            // position parameters, which should be made read-only) but allows the user
            // to [drag the camera and device on a pre-Play representation of the robot
            // and to] drag the robot and thereby change its approach position. The
            // Play button erases the pre-Play representation and starts the animation
            // from the game's starting position.

            //**TODO At this point the "Position" button should show the robot in its approach
            // position, the approach zone, and the camera's field of view. Later allow drag
            // adjustments to the position of the robot and, possibly, to the positions of
            // the camera and device on the robot.

            // Mark a portion of the field as the approach zone, i.e. the point at which the robot
            // stops in front of the backdrop. The zone defines the outer limits of the robot.
            //**TODO In the future you'll have to freeze the width and height of the robot (and
            // maybe not all of the other parameters) because changes to the width and height
            // don't make much sense and would have too many ramifications.
            double approachZoneX = StartParameterValidation.ROBOT_POSITION_AT_BACKDROP_X_MIN * FieldFXCenterStageBackdropLG.PX_PER_INCH -
                    (robotWidthIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2;
            double approachZoneY = StartParameterValidation.ROBOT_POSITION_AT_BACKDROP_Y_MIN * FieldFXCenterStageBackdropLG.PX_PER_INCH -
                    (robotHeightIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2;
            double approachZoneWidth = ((StartParameterValidation.ROBOT_POSITION_AT_BACKDROP_X_MAX * FieldFXCenterStageBackdropLG.PX_PER_INCH +
                    (robotWidthIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2)) - approachZoneX;
            double approachZoneHeight = ((StartParameterValidation.ROBOT_POSITION_AT_BACKDROP_Y_MAX * FieldFXCenterStageBackdropLG.PX_PER_INCH +
                    (robotHeightIn * FieldFXCenterStageBackdropLG.PX_PER_INCH) / 2)) - approachZoneY;
            Rectangle approachZone = new Rectangle(approachZoneX, approachZoneY, approachZoneWidth, approachZoneHeight);
            approachZone.setId("ApproachZone");
            approachZone.setFill(Color.TRANSPARENT);
            approachZone.setStroke(Color.BLACK);
            approachZone.getStrokeDashArray().addAll(5.0);
            approachZone.setStrokeWidth(2.0);
            field.getChildren().add(approachZone);

            // Collect start parameters.
            double cameraCenterFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.CAMERA_CENTER_FROM_ROBOT_CENTER);
            double cameraOffsetFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.CAMERA_OFFSET_FROM_ROBOT_CENTER);
            double deviceCenterFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.DEVICE_CENTER_FROM_ROBOT_CENTER);
            double deviceOffsetFromRobotCenter = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.DEVICE_OFFSET_FROM_ROBOT_CENTER);
            double robotPositionAtBackdropX = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_POSITION_AT_BACKDROP_X) * FieldFXCenterStageBackdropLG.PX_PER_INCH;
            double robotPositionAtBackdropY = startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.ROBOT_POSITION_AT_BACKDROP_Y) * FieldFXCenterStageBackdropLG.PX_PER_INCH;
            Integer targetAprilTag = controller.april_tag_spinner.getValue();
            String radioButtonText = ((RadioButton) controller.approach_toggle.getSelectedToggle()).getText();

            //**TODO Show the robot in its approach position opposite the backdrop.
            // Starting position is the default of 36.0, 36.0
            /*
                        centerStageRobot = new RobotFXCenterStageLG(robotWidthIn, robotHeightIn, Color.GREEN,
                    cameraCenterFromRobotCenter, cameraOffsetFromRobotCenter, deviceCenterFromRobotCenter, deviceOffsetFromRobotCenter,
                    startingPosition, 0.0);

            Group robot = centerStageRobot.getRobot();
            field.getChildren().add(robot);
             */

            //**TODO As an experiment draw a 78 degree camera field of view.
            /*
            // tan 39 degrees  = opposite / adjacent
            double halfFOVTan = Math.tan(Math.toRadians(39.0));
            double fovAdjacent = cameraAdjacent; // same
            double halfFOVOpposite = halfFOVTan * fovAdjacent;
            Line lineHalfFOVRight = new Line(cameraFaceX, cameraFaceY, cameraFaceX + halfFOVOpposite, aprilTagCenterY);
            lineHalfFOVRight.setId("lineHalfFOVRight");
            lineHalfFOVRight.setStroke(Color.AQUA);
            lineHalfFOVRight.getStrokeDashArray().addAll(10.0);
            lineHalfFOVRight.setStrokeWidth(3.0);
            field.getChildren().add(lineHalfFOVRight);

            Line lineHalfFOVLeft = new Line(cameraFaceX, cameraFaceY, cameraFaceX - halfFOVOpposite, aprilTagCenterY);
            lineHalfFOVLeft.setId("lineHalfFOVLeft");
            lineHalfFOVLeft.setStroke(Color.AQUA);
            lineHalfFOVLeft.getStrokeDashArray().addAll(10.0);
            lineHalfFOVLeft.setStrokeWidth(3.0);
            field.getChildren().add(lineHalfFOVLeft);
            */

            //**TODO Then, when the Play button is first hit, erase the positioning robot and the FOV lines.

            centerStageRobot = new RobotFXCenterStageLG(robotWidthIn, robotHeightIn, Color.GREEN,
                    cameraCenterFromRobotCenter, cameraOffsetFromRobotCenter, deviceCenterFromRobotCenter, deviceOffsetFromRobotCenter,
                    startingPosition, startingRotation);

            Group robot = centerStageRobot.getRobot();
            field.getChildren().add(robot);


            //**TODO Move to DeviceToTargetAnimation ...
            System.out.println("Alliance " + alliance);
            System.out.println("Camera center from robot center " + cameraCenterFromRobotCenter);
            System.out.println("Camera offset from robot center " + cameraOffsetFromRobotCenter);
            System.out.println("Device center from robot center " + deviceCenterFromRobotCenter);
            System.out.println("Device offset from robot center " + deviceOffsetFromRobotCenter);
            System.out.println("Position at backdrop " + robotPositionAtBackdropX + ", y " + robotPositionAtBackdropY);
            System.out.println("AprilTag Id " + targetAprilTag);
            System.out.println("Approach " + radioButtonText);

            // Animate the movements of the robot from its starting position to
            // its final position in which the delivery device is aligned with
            // the target.
            DeviceToTargetAnimation animation = new DeviceToTargetAnimation(controller, field, centerStageRobot, startParameterValidation);
            animation.runDeviceToTargetAnimation(alliance, animationButton);
        });

        animationButton.setOnAction(event.get());
    }

    //**TODO Fix spacing - either here or in fxml.
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