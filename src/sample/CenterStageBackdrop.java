package sample;

import javafx.animation.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

// Combination of
// https://www.infoworld.com/article/2074529/javafx-2-animation--path-transitions.html
// and
// https://docs.oracle.com/javafx/2/animations/basics.htm#CJAJJAGI
public class CenterStageBackdrop extends Application {

    private enum Corners {TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT}

    private SimulatorController controller;
    private StartParameterValidation startParameterValidation;
    private RobotFXCenterStageLG centerStageRobot;

    //**TODO Show all positions in FTC field coordinates? Or at least report the field coordinates.
    @Override
    public void start(final Stage pStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("simulatorLG.fxml"));
        BorderPane root = fxmlLoader.load();
        controller = fxmlLoader.getController();
        Pane field = controller.field;

        pStage.setTitle("FTC Center Stage Backdrop and AprilTags");
        Scene rootScene = new Scene(root);
        pStage.setScene(rootScene);
        pStage.show(); // show the empty field

        String allianceString = allianceSelection(pStage);
        RobotConstants.Alliance alliance = RobotConstants.Alliance.valueOf(allianceString);
        //**TODO Or send the Stage and primary Scene to allianceSelection and let it restore
        // the root scene.
        pStage.setScene(rootScene); // reset to primary Pane

        FieldFXCenterStageBackdropLG fieldCenterStageBackdrop = new FieldFXCenterStageBackdropLG(alliance, field);

        // Show the alliance id in its color.
        controller.alliance.setText(allianceString);
        controller.alliance.setFont(Font.font("System", FontWeight.BOLD, 14));
        Color allianceColor = (alliance == RobotConstants.Alliance.BLUE) ? Color.BLUE : Color.RED;
        controller.alliance.setTextFill(allianceColor); // or jewelsea setStyle("-fx-text-inner-color: red;");

        //**TODO ??Need a way to read parameters from an XML file and then write them
        // back out?? Only the robot dimensions, camera placement, device placement.
        // Parse and validate the start parameters that have a range of double values.
        startParameterValidation = new StartParameterValidation(controller);

        SpinnerValueFactory.IntegerSpinnerValueFactory spinnerValueFactory;
        if (alliance == RobotConstants.Alliance.BLUE)
            spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 3);
        else // RED
            spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(4, 6);

        controller.april_tag_spinner.setValueFactory(spinnerValueFactory);

        // Show the play button now but do not start the animation until
        // all start parameters have been validated.
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

            playPauseButton.removeEventHandler(ActionEvent.ACTION, event.get());

            // Freeze the start parameters after the Play button has been hit.
            controller.start_parameters.setDisable(true);

            //**TODO Here it looks like the position of the robot is that
            // of the upper left corner; Paths use the center point.

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

            centerStageRobot = new RobotFXCenterStageLG(robotWidthIn, robotHeightIn, Color.GREEN,
                    startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.CAMERA_CENTER_FROM_ROBOT_CENTER),
                    startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.CAMERA_OFFSET_FROM_ROBOT_CENTER),
                    startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.DEVICE_CENTER_FROM_ROBOT_CENTER),
                    startParameterValidation.getStartParameter(StartParameterValidation.StartParameter.DEVICE_OFFSET_FROM_ROBOT_CENTER),
                    startingPosition, startingRotation);

            Group robot = centerStageRobot.getRobot();
            field.getChildren().add(robot);

            // Animate the movements of the robot from its starting position
            // to its final position in which the delivery device is aligned with
            // the target.
            //**TODO System.out.println() -- all start parameters.

            DeviceToTargetAnimation animation = new DeviceToTargetAnimation(controller, field, centerStageRobot, startParameterValidation);
            animation.runDeviceToTargetAnimation(alliance, playPauseButton);
        });

        playPauseButton.setOnAction(event.get());
    }

    //**TODO What I really want is a RadioButtonDialog, which doesn't exist.
    // But it looks like you may be able make a custom Dialog with
    // RadioButton(s)/Toggle group inside it. But this will take some work.
    private String allianceSelection(Stage pStage) {
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

        return allianceSelection;
    }

    //**TODO Not using these but don't want to lose track of them.
    // Given the screen coordinates of a corner of a rectangle and its rotation angle,
    // get the screen coordinates of the center of the rectangle.
    // The angle is in the FTC range (0 to +180 not inclusive, 0 to -180 inclusive) but
    // the JavaFX orientation (CW positive, CCW negative).
    // From https://stackoverflow.com/questions/60573374/finding-the-midpoint-of-the-rotated-rectangle
    private Point2D computeRotatedCentroid(double pX, double pY, double pWidth, double pHeight, double pAngle) {
        double centerX = 0.5 * pWidth;
        double centerY = 0.5 * pHeight;
        double angleRadians = Math.toRadians(pAngle);

        double cosAngle = Math.cos(angleRadians);
        double sinAngle = Math.sin(angleRadians);

        double finalCx = pX + centerX * cosAngle - centerY * sinAngle;
        double finalCy = pY + centerX * sinAngle + centerY * cosAngle;

        return new Point2D(finalCx, finalCy);
    }

    // Get the coordinates of all 4 corners of a rotated rectangle.
    // The coordinates of the center of the rectangle are JavaFX screen coordinates.
    // The angle is in the FTC range (0 to +180 not inclusive, 0 to -180 inclusive) but the JavaFX orientation
    // (CW positive, CCW negative).
    // From https://stackoverflow.com/questions/41898990/find-corners-of-a-rotated-rectangle-given-its-center-point-and-rotation
    // See also https://math.stackexchange.com/questions/126967/rotating-a-rectangle-via-a-rotation-matrix
    private Map<Corners, Point2D> robotBodyCornerCoordinates(double pCenterX, double pCenterY, double pWidth, double pHeight, double pAngle) {
        Map<Corners, Point2D> cornerMap = new HashMap<>();

        // The formula assumes FTC orientation (CCW positive, CW negative) and Cartesian coordinates.
        pAngle = -pAngle;
        double angleRadians = Math.toRadians(pAngle);
        pCenterY = FieldFX.FIELD_HEIGHT - pCenterY; // Convert screen Y coordinate to Cartesian

        // TOP LEFT VERTEX
        double topLeftX = pCenterX - ((pWidth / 2) * Math.cos(angleRadians)) - ((pHeight / 2) * Math.sin(angleRadians));
        double topLeftY = pCenterY - ((pWidth / 2) * Math.sin(angleRadians)) + ((pHeight / 2) * Math.cos(angleRadians));
        topLeftY = FieldFX.FIELD_HEIGHT - topLeftY;
        cornerMap.put(Corners.TOP_LEFT, new Point2D(topLeftX, topLeftY));

        // TOP RIGHT VERTEX
        double topRightX = pCenterX + ((pWidth / 2) * Math.cos(angleRadians)) - ((pHeight / 2) * Math.sin(angleRadians));
        double topRightY = pCenterY + ((pWidth / 2) * Math.sin(angleRadians)) + ((pHeight / 2) * Math.cos(angleRadians));
        topRightY = FieldFX.FIELD_HEIGHT - topRightY;
        cornerMap.put(Corners.TOP_RIGHT, new Point2D(topRightX, topRightY));

        // BOTTOM RIGHT VERTEX
        double bottomRightX = pCenterX + ((pWidth / 2) * Math.cos(angleRadians)) + ((pHeight / 2) * Math.sin(angleRadians));
        double bottomRightY = pCenterY + ((pWidth / 2) * Math.sin(angleRadians)) - ((pHeight / 2) * Math.cos(angleRadians));
        bottomRightY = FieldFX.FIELD_HEIGHT - bottomRightY;
        cornerMap.put(Corners.BOTTOM_RIGHT, new Point2D(bottomRightX, bottomRightY));

        // BOTTOM LEFT VERTEX
        double bottomLeftX = pCenterX - ((pWidth / 2) * Math.cos(angleRadians)) + ((pHeight / 2) * Math.sin(angleRadians));
        double bottomLeftY = pCenterY - ((pWidth / 2) * Math.sin(angleRadians)) - ((pHeight / 2) * Math.cos(angleRadians));
        bottomLeftY = FieldFX.FIELD_HEIGHT - bottomLeftY;
        cornerMap.put(Corners.BOTTOM_LEFT, new Point2D(bottomLeftX, bottomLeftY));

        return cornerMap;
    }

    // Generic version: find the lowest value in a map according to the criteria defined in the Comparator.
    // From https://stackoverflow.com/questions/37348462/find-minimum-value-in-a-map-java
    private <K, V> V minMapValue(Map<K, V> pMap, Comparator<V> pComp) {
        return pMap.values().stream().min(pComp).get();
    }

    // Specific version: in a Map<Corners, Point2D> find the key/value pair with the lowest y-coordinate
    // value.
    private Point2D minYCorner(Map<Corners, Point2D> pCornerMap) {
        return pCornerMap.values().stream().min(Comparator.comparingDouble(Point2D::getY)).get();
    }

}