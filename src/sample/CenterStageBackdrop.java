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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.SpinnerValueFactory;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

// Combination of
// https://www.infoworld.com/article/2074529/javafx-2-animation--path-transitions.html
// and
// https://docs.oracle.com/javafx/2/animations/basics.htm#CJAJJAGI
public class CenterStageBackdrop extends Application {

    private enum Corners {TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT}

    SimulatorController controller;
    StartParameterValidation startParameterValidation;

    //**TODO Do you really need device center from robot center? Yes, for visualization.
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
        //**TODO Or send the primary Scene to allianceSelection and let it restore
        pStage.setScene(rootScene); // reset to primary Pane

        FieldFXCenterStageBackdropLG fieldCenterStageBackdrop = new FieldFXCenterStageBackdropLG(alliance, field);

        // Show the alliance id in its color.
        controller.alliance_id.setText(allianceString);
        controller.alliance_id.setFont(Font.font("System", FontWeight.BOLD, 14));
        Color allianceColor = (alliance == RobotConstants.Alliance.BLUE) ? Color.BLUE : Color.RED;
        controller.alliance_id.setTextFill(allianceColor); // or jewelsea setStyle("-fx-text-inner-color: red;");

        // Show the robot on the field ina default starting position.
        //**TODO how to specify variable information: dimensions of the robot,
        // position of the camera and device on the robot: user input box on
        // startup, then drag-and-drop OR choose a default position then drag-
        // and-drop.
        //**TODO How to specify the position of the robot in front of the
        // backdrop: user input box on startup, then drag-and-drop OR choose
        // a default position then drag-and-drop.
        //**TODO Don't need OpMode - user selects AprilTag
        //**TODO Pass in scaling factor for tile, robot size; e.g. 100px squares vs 200
        //**TODO When you use the Point2D below the body of the robot is lined up with
        // the grid at y400 but the wheels are above the grid line. Fix this ...

        //**TODO Are you sure that a Group is the right container for the robot?
        //**TODO DIFFERENT for BLUE and RED ... Here it looks like the position is of the
        // upper left corner of the robot; Paths use the center point.
        //**TODO Maybe the initial position has to anticipate the 90 degree rotation ...
        RobotFXCenterStageLG centerStageRobot;
        if (alliance == RobotConstants.Alliance.BLUE) {
            centerStageRobot = new RobotFXCenterStageLG(
                new Point2D(FieldFXCenterStageBackdropLG.PX_PER_INCH * 1.5,
                        FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 2 + FieldFXCenterStageBackdropLG.PX_PER_INCH * 1.5),
                90.0, Color.GREEN);
    } else { // RED
            centerStageRobot = new RobotFXCenterStageLG(
                    new Point2D(FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 2 + (FieldFXCenterStageBackdropLG.TILE_DIMENSIONS - (RobotFXLG.ROBOT_BODY_WIDTH + (RobotFXLG.WHEEL_WIDTH * 2))) - FieldFXCenterStageBackdropLG.PX_PER_INCH * 1.5,
                            FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 2 + FieldFXCenterStageBackdropLG.PX_PER_INCH * 1.5),
                    -90.0, Color.GREEN);
        }

        // Parse and validate the start parameters that have a range of double values.
        startParameterValidation = new StartParameterValidation(controller);

        SpinnerValueFactory.IntegerSpinnerValueFactory spinnerValueFactory;
        if (alliance == RobotConstants.Alliance.BLUE)
            spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 3);
        else // RED
            spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(4, 6);
        controller.april_tag_spinner_id.setValueFactory(spinnerValueFactory);

        // Show the play button now but do not start the animation until
        // all start parameters have been validated.
        Button playButton = setPlayButton(field, alliance);
        EventHandler<ActionEvent> event = e -> {
            if (!startParameterValidation.allStartParametersValid()) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Invalid request to Play the animation");
                errorAlert.setContentText("Not all start parameters have been set correctly");
                errorAlert.showAndWait();
                return;
            }

            Group robot = centerStageRobot.getRobot();
            field.getChildren().add(robot);

            applyAnimation(robot, alliance);
        };

        playButton.setOnAction(event);
    }

    //**TODO What I really want is a RadioButtonDialog, which doesn't exist.
    // But it looks like you may be able make a custom Dialog with
    // RadioButton(s)/Toggle group inside it. But this will take some work.
    private String allianceSelection(Stage pStage) throws IOException {
        /*
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("allianceToggle.fxml"));
        AnchorPane root = fxmlLoader.load();
        AllianceToggleController controller = fxmlLoader.getController();
        */

        Button okButton = new Button("OK");

        // Items for the dialog.
        String alliances[] = {"BLUE", "RED"};
        ChoiceDialog allianceDialog = new ChoiceDialog(alliances[0], alliances);

        allianceDialog.setHeaderText("Select alliance and confirm, fill in start parameters, hit Play");
        allianceDialog.setContentText("Please select your alliance");
        allianceDialog.showAndWait();

        // get the selected item
        String allianceSelection = (String) allianceDialog.getSelectedItem();

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

    //**TODO Note that some parameters are dependent on others, e.g
    // are dependent on the robot's dimensions.
    private void validateStartParameters() {
        StartParameterValidation startParameterValidation = new StartParameterValidation(controller);

    }

    //**TODO From FTCAutoSimulator/RobotSimulator
        // Set up the Play button.
    private Button setPlayButton(Pane pFieldPane, RobotConstants.Alliance pAlliance) {
        Button playButton = new Button("Play");
        playButton.setFont(Font.font("Verdana", FontWeight.BOLD, 12));

        // Position the button on the opposite side of the field from
        // the selected alliance.
        //**TODO Get the offsets from the button itself.
        Bounds buttonBoundsLocal = playButton.getBoundsInLocal();
        final double buttonOffsetX = 50;
        final double buttonOffsetY = 50;

        playButton.setLayoutY(FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 3 - buttonOffsetY);
        if (pAlliance == RobotConstants.Alliance.BLUE)
            playButton.setLayoutX((FieldFXCenterStageBackdropLG.TILE_DIMENSIONS * 3) - FieldFXCenterStageBackdropLG.FIELD_OUTSIDE_BORDER_SIZE - buttonOffsetX);
        else
            playButton.setLayoutX(FieldFXCenterStageBackdropLG.FIELD_OUTSIDE_BORDER_SIZE + buttonOffsetX);

        pFieldPane.getChildren().add(playButton);

        return playButton;
    }

    private void applyAnimation(Group pRobot, RobotConstants.Alliance pAlliance) {

        //## As a demonstration start the robot facing inward from the BLUE
        // alliance wall and make the robot follow a CubicCurve path while
        // simultaneously rotating -90 degrees to face the backdrop.

        //!! Who knew that this adjustment is necessary?
        // https://stackoverflow.com/questions/29594707/moving-a-button-to-specified-coordinates-in-javafx-with-a-path-transition-using
        // But if the initial rotation is 90.0 instead of 0.0 the starting position is not correct.
        // Instead of getLayoutBounds() you have to use getBoundsInParent().
        //double xOffsetInParent = pRobot.getBoundsInParent().getWidth() / 2;
        //double yOffsetInParent = pRobot.getBoundsInParent().getHeight() / 2;

        //!! However, I noticed the use of localToScene(() in some code below -
        // this is more like it. By the way, this is the *center* of the robot.
        Point2D loc = pRobot.localToScene(pRobot.getBoundsInParent().getCenterX(), pRobot.getBoundsInParent().getCenterY());

        Path path = new Path();
        path.getElements().add(new MoveTo(loc.getX(), loc.getY()));

        //**TODO The curves are a proof-of-concept. They will be different depending
        // on the user's selection for the final position in front of the backdrop.
        // CubicCurveTo constructor parameters: controlX1, controlX2, controly1, controly2, endX, endY
        float rotation;
        if (pAlliance == RobotConstants.Alliance.BLUE) {
            path.getElements().add(new CubicCurveTo(400, 300, 300, 300, 200, 275));
            rotation = -90.0f;
        }
        else { // RED
            path.getElements().add(new CubicCurveTo(200, 300, 300, 300, 400, 275));
            rotation = 90.0f;
        }

        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(3000));
        pathTransition.setPath(path);
        pathTransition.setNode(pRobot);

        RotateTransition rotateTransition =
                new RotateTransition(Duration.millis(3000), pRobot);
        rotateTransition.setByAngle(rotation);

        // Follow the cubic curve and rotate in parallel.
        ParallelTransition parallelT = new ParallelTransition(pathTransition, rotateTransition);
        parallelT.setCycleCount(5);
        parallelT.setAutoReverse(true);
        parallelT.setOnFinished(event -> { //**TODO See OneNote - stackoverflow answer from jewelsea
            pRobot.setLayoutX(pRobot.getLayoutX() + pRobot.getTranslateX());
            pRobot.setLayoutY(pRobot.getLayoutY() + pRobot.getTranslateY());
            pRobot.setTranslateX(0);
            pRobot.setTranslateY(0);
        });
        parallelT.play();

        //**TODO Get the angle and distance from the camera to the
        // selected AprilTag and display. Draw a line from the camera
        // to the AprilTag. Get the angle and distance from the center
        // of the robot to the selected AprilTag and display. Draw a
        // line from the center of the robot to the AprilTag. Get the
        // angle and distance from the device to the selected AprilTag
        // and display. Draw a line from the device to the AprilTag.

        /*
        TranslateTransition tt = new TranslateTransition(Duration.millis(2000));
        tt.setNode(robot);
        tt.setToX(150f); // simple strafe
        tt.setOnFinished(event -> { //**TODO See OneNote - stackoverflow answer from jewelsea
            robot.setLayoutX(robot.getLayoutX() + robot.getTranslateX());
            robot.setLayoutY(robot.getLayoutY() + robot.getTranslateY());
            robot.setTranslateX(0);
            robot.setTranslateY(0);

            // But mixed in with the above also do this:
            Rectangle robotBody = (Rectangle) pRobot.lookup("#robotBodyId");
            Point2D rbCoord = robotBody.localToScene(robotBody.getX(), robotBody.getY());
            System.out.println("Position after path following x " + rbCoord.getX() + ", y " + rbCoord.getY());
            System.out.println("JavaFX rotation after path " + pRobot.getRotate());

            double robotRotation = pRobot.getRotate() + 90.0;
            System.out.println("Robot rotation after path " + robotRotation);

            Point2D centroid = computeRotatedCentroid(rbCoord.getX(), rbCoord.getY(), RobotFX.ROBOT_WIDTH, RobotFX.ROBOT_HEIGHT, robotRotation);
            System.out.println("Centroid x " + centroid.getX() + ", y " + centroid.getY());
        });

        //**TODO Get the robot's Rectangle by Id then get its centroid by:
        //             Point2D centroid = computeRotatedCentroid(rbCoord.getX(), rbCoord.getY(), RobotFX.ROBOT_WIDTH, RobotFX.ROBOT_HEIGHT, robotRotation);
        // then strafe so that the centroid is opposite the centroid of AprilTag 3.
         */

    }

    private PathTransition generatePathTransition(Group pRobot, final Path path) {
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(4000));
        pathTransition.setPath(path);
        pathTransition.setNode(pRobot);
        pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        pathTransition.setCycleCount(1);
        pathTransition.setAutoReverse(false);
        pathTransition.setOnFinished(event -> {
            Rectangle robotBody = (Rectangle) pRobot.lookup("#robotBodyId");
            Point2D rbCoord = robotBody.localToScene(robotBody.getX(), robotBody.getY());
            System.out.println("Position after path following x " + rbCoord.getX() + ", y " + rbCoord.getY());
            System.out.println("JavaFX rotation after path " + pRobot.getRotate());

            double robotRotation = pRobot.getRotate() + 90.0;
            System.out.println("Robot rotation after path " + robotRotation);

            Point2D centroid = computeRotatedCentroid(rbCoord.getX(), rbCoord.getY(), RobotFX.ROBOT_WIDTH, RobotFX.ROBOT_HEIGHT, robotRotation);
            System.out.println("Centroid x " + centroid.getX() + ", y " + centroid.getY());

            Map<Corners, Point2D> cornerMap = robotBodyCornerCoordinates(centroid.getX(), centroid.getY(), RobotFX.ROBOT_WIDTH, RobotFX.ROBOT_HEIGHT, robotRotation);
            System.out.println("Robot body top left " + cornerMap.get(Corners.TOP_LEFT).getX() + " y " + cornerMap.get(Corners.TOP_LEFT).getY());
            System.out.println("Top right " + cornerMap.get(Corners.TOP_RIGHT).getX() + " y " + cornerMap.get(Corners.TOP_RIGHT).getY());
            System.out.println("Bottom right " + cornerMap.get(Corners.BOTTOM_RIGHT).getX() + " y " + cornerMap.get(Corners.BOTTOM_RIGHT).getY());
            System.out.println("Bottom left " + cornerMap.get(Corners.BOTTOM_LEFT).getX() + " y " + cornerMap.get(Corners.BOTTOM_LEFT).getY());

            // https://stackoverflow.com/questions/26513747/efficient-way-to-find-min-value-in-map
            // Obtain the entry with the minimum value:
            Map.Entry<Corners, Point2D> entryWithMinValue = Collections.min(
                    cornerMap.entrySet(), Map.Entry.comparingByValue(Comparator.comparingDouble(Point2D::getY)));
            System.out.println("Minimum y coordinate " + entryWithMinValue);

            // Or directly obtain the key, if you only need that:
            //String keyWithMinValue = Collections.min(
            //        myMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            //System.out.println(keyWithMinValue);

        });
        return pathTransition;
    }

    private void translateAndRotate(Group pGroup) {
        final Rectangle rect2 = new Rectangle(120, 210, 60, 60);
        rect2.setFill(Color.DARKCYAN);
        pGroup.getChildren().add(rect2);

        rect2.xProperty().addListener(xValue -> {
            System.out.println("XProperty " + xValue);
        });

        PauseTransition pauseTransition = new PauseTransition(Duration.millis(2000));

        TranslateTransition translateNorth = new TranslateTransition();
        translateNorth.setNode(rect2);
        translateNorth.setByY(-90);
        translateNorth.setDuration(Duration.seconds(2));
        translateNorth.setOnFinished(event -> {
            Point2D rbCoord = rect2.localToScene(rect2.getX(), rect2.getY());
            System.out.println("Position after straight line motion x " + rbCoord.getX() + ", y " + rbCoord.getY());
        });

        RotateTransition rotate90 =
                new RotateTransition(Duration.seconds(2), rect2);
        rotate90.setByAngle(90.0);
        rotate90.setOnFinished(event -> {
            Point2D rbCoord = rect2.localToScene(rect2.getX(), rect2.getY());
            System.out.println("Position after turn x " + rbCoord.getX() + ", y " + rbCoord.getY());
            System.out.println("Rotation " + rect2.getRotate());
        });

        TranslateTransition translateWest = new TranslateTransition();
        translateWest.setNode(rect2);
        translateWest.setByX(90);
        translateWest.setDuration(Duration.seconds(2));
        translateWest.setOnFinished(event -> {
            Point2D rbCoord = rect2.localToScene(rect2.getX(), rect2.getY());
            System.out.println("Position after straight line motion x " + rbCoord.getX() + ", y " + rbCoord.getY());
        });

        SequentialTransition sequential = new SequentialTransition();
        sequential.getChildren().add(pauseTransition);
        sequential.getChildren().add(translateNorth);
        sequential.getChildren().add(rotate90);
        sequential.getChildren().add(translateWest);
        sequential.play();
    }

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

    // Generic version: find the lowest value in a map according to the criteria defined in the Camparator.
    // From https://stackoverflow.com/questions/37348462/find-minimum-value-in-a-map-java
    private static <K, V> V minMapValue(Map<K, V> pMap, Comparator<V> pComp) {
        return pMap.values().stream().min(pComp).get();
    }

    // Specific version: in a Map<Corners, Point2D> find the key/value pair with the lowest y-coordinate
    // value.
    private static Point2D minYCorner(Map<Corners, Point2D> pCornerMap) {
        return pCornerMap.values().stream().min(Comparator.comparingDouble(Point2D::getY)).get();
    }

}