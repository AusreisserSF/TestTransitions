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

            //**TODO How do you freeze the start parameters after the Play button has been
            // hit?

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

            runAnimation(alliance, field, robot, playPauseButton);
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

    //**TODO Put all of this into a separate class. Then make AtomicReferences class fields.
    //**TODO There are four triangles involved in the animation of the robot: camera to target,
    // robot center to target, robot center to device, device to target.
    //**TODO Make naming conform to these triangles (or abbreviations).
    private void runAnimation(RobotConstants.Alliance pAlliance, Pane pField, Group pRobot, Button pPlayPauseButton) {

        //## As a demonstration start the robot facing inward from the BLUE
        // alliance wall and make the robot follow a CubicCurve path while
        // simultaneously rotating -90 degrees to face the backdrop.

        //!! I noticed the use of localToScene(() in some code from the FTCSimulator -
        // this is more like it. By the way, this is the *center* of the robot.
        Point2D loc = pRobot.localToScene(pRobot.getBoundsInParent().getCenterX(), pRobot.getBoundsInParent().getCenterY());

        Path path = new Path();
        path.getElements().add(new MoveTo(loc.getX(), loc.getY()));

        //**TODO The curves are a proof-of-concept. They will be different depending
        // on the user's selection for the final position in front of the backdrop.
        // CubicCurveTo constructor parameters: controlX1, controlX2, controlY1, controlY2, endX, endY
        float rotation;
        if (pAlliance == RobotConstants.Alliance.BLUE) {
            path.getElements().add(new CubicCurveTo(400, 300, 300, 300, 200, 275));
            rotation = -90.0f;
        } else { // RED
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
        rotateTransition.setOnFinished(event -> System.out.println("Angle after initial rotation " + pRobot.getRotate()));

        AtomicReference<Double> robotCoordX = new AtomicReference<>((double) 0);
        AtomicReference<Double> robotCoordY = new AtomicReference<>((double) 0);
        AtomicReference<Double> deviceCenterX = new AtomicReference<>((double) 0);
        AtomicReference<Double> deviceCenterY = new AtomicReference<>((double) 0);
        AtomicReference<Double> aprilTagCenterX = new AtomicReference<>((double) 0);
        AtomicReference<Double> aprilTagCenterY = new AtomicReference<>((double) 0);

        //## The TranslateTransition for the final strafe must be declared before the
        // ParallelTransition, i.e. out of the time sequence, because the distance to
        // strafe is only known after the ParallelTransition is complete.
        TranslateTransition ttStrafe = new TranslateTransition(Duration.millis(2000));
        ttStrafe.setNode(pRobot);
        ttStrafe.setOnFinished(event -> { //**TODO See OneNote - stackoverflow answer from jewelsea
            System.out.println("After strafe layoutX " + pRobot.getLayoutX() + ", translateX " + pRobot.getTranslateX());
            pRobot.setLayoutX(pRobot.getLayoutX() + pRobot.getTranslateX());
            pRobot.setLayoutY(pRobot.getLayoutY() + pRobot.getTranslateY());
            pRobot.setTranslateX(0);
            pRobot.setTranslateY(0);

            // Draw a line from the device to the AprilTag.
            Circle deviceOnRobot = (Circle) pRobot.lookup("#" + pRobot.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
            Point2D deviceCoord = deviceOnRobot.localToScene(deviceOnRobot.getCenterX(), deviceOnRobot.getCenterY());
            Line lineDAT = new Line(deviceCoord.getX(), deviceCoord.getY(), aprilTagCenterX.get(), aprilTagCenterY.get());
            lineDAT.setId("lineDAT");
            lineDAT.setStroke(Color.YELLOW);
            lineDAT.getStrokeDashArray().addAll(10.0);
            lineDAT.setStrokeWidth(3.0);
            pField.getChildren().add(lineDAT);
            System.out.println("Distance from device to AprilTag " + (aprilTagCenterY.get() - deviceCoord.getY()));

            Bounds robotBP = pRobot.getBoundsInParent();
            System.out.println("Robot position after strafe x " + robotBP.getCenterX() + ", y " + robotBP.getCenterY());
        });

        RadioButton selectedRadioButton = (RadioButton) controller.approach_toggle.getSelectedToggle();
        String radioButtonText = selectedRadioButton.getText();

        //## The RotateTransition for the final rotation must be declared before the
        // ParallelTransition, i.e. out of the time sequence, because the angle to
        // rotate is only known after the ParallelTransition is complete.
        RotateTransition rotateDeviceTowardsAprilTag = new RotateTransition(Duration.seconds(2));
        rotateDeviceTowardsAprilTag.setNode(pRobot);
        rotateDeviceTowardsAprilTag.setOnFinished(event -> {
            // The following made no difference ...
            /*
            pRobot.setLayoutX(pRobot.getLayoutX() + pRobot.getTranslateX());
            pRobot.setLayoutY(pRobot.getLayoutY() + pRobot.getTranslateY());
            pRobot.setTranslateX(0);
            pRobot.setTranslateY(0);
            */

            System.out.println("Angle after rotation " + pRobot.getRotate());

            //**NOTHING TODO - this worked!
            // Draw a line from the device to the AprilTag.
            Circle deviceOnRobot = (Circle) pRobot.lookup("#" + pRobot.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
            Point2D deviceCoord = deviceOnRobot.localToScene(deviceOnRobot.getCenterX(), deviceOnRobot.getCenterY());
            Line lineDH = new Line(deviceCoord.getX(), deviceCoord.getY(), aprilTagCenterX.get(), aprilTagCenterY.get());
            lineDH.setId("lineDCH");
            lineDH.setStroke(Color.YELLOW);
            lineDH.getStrokeDashArray().addAll(10.0);
            lineDH.setStrokeWidth(3.0);
            pField.getChildren().add(lineDH);
        });

        // Follow the cubic curve and rotate in parallel.
        ParallelTransition parallelT = new ParallelTransition(pathTransition, rotateTransition);
        parallelT.setOnFinished(event -> { //**TODO See OneNote - stackoverflow answer from jewelsea
            pRobot.setLayoutX(pRobot.getLayoutX() + pRobot.getTranslateX());
            pRobot.setLayoutY(pRobot.getLayoutY() + pRobot.getTranslateY());
            pRobot.setTranslateX(0);
            pRobot.setTranslateY(0);

            Bounds robotBP = pRobot.getBoundsInParent();
            robotCoordX.set(robotBP.getCenterX());
            robotCoordY.set(robotBP.getCenterY());

            System.out.println("Robot center after ParallelTransition x " + robotCoordX + ", y " + robotCoordY);
            System.out.println("Robot rotation after ParallelTransition x " + pRobot.getRotate());

            //## All of these coordinates and calculations can only be made after
            // the ParallelTranstion is complete, i.e. now.
            // complete; further action must be taken here.
            Rectangle cameraOnRobot = (Rectangle) pRobot.lookup("#" + pRobot.getId() + "_" + RobotFXCenterStageLG.CAMERA_ON_ROBOT_ID);
            Point2D cameraCoord = cameraOnRobot.localToScene(cameraOnRobot.getX(), cameraOnRobot.getY());

            // The returned coordinates of the objects are those of the upper left-hand
            // corner. We want to draw a line from the face of the camera to the center
            // of the AprilTag.
            double cameraFaceX = cameraCoord.getX() + cameraOnRobot.getWidth() / 2;
            double cameraFaceY = cameraCoord.getY();
            System.out.println("Camera face center x " + cameraFaceX + ", y " + cameraFaceY);

            Circle deviceOnRobot = (Circle) pRobot.lookup("#" + pRobot.getId() + "_" + RobotFXCenterStageLG.DEVICE_ON_ROBOT_ID);
            Point2D deviceCoord = deviceOnRobot.localToScene(deviceOnRobot.getCenterX(), deviceOnRobot.getCenterY());
            deviceCenterX.set(deviceCoord.getX());
            deviceCenterY.set(deviceCoord.getY());

            // Get the coordinates of the target AprilTag.
            Integer targetAprilTag = controller.april_tag_spinner.getValue();
            Rectangle aprilTag = (Rectangle) pField.lookup("#" + FieldFXCenterStageBackdropLG.APRIL_TAG_ID + targetAprilTag);
            Point2D aprilTagCoord = aprilTag.localToScene(aprilTag.getX(), aprilTag.getY());

            aprilTagCenterX.set(aprilTagCoord.getX() + aprilTag.getWidth() / 2);
            aprilTagCenterY.set(aprilTagCoord.getY() + aprilTag.getHeight() / 2);
            System.out.println("AprilTag center x " + aprilTagCenterX + ", y " + aprilTagCenterY);

            // Support a strafe that positions the delivery device opposite the AT.
            // Positive: strafe to the left; negative: strafe to the right.
            double distanceToStrafe = aprilTagCenterX.get() - deviceCenterX.get();
            System.out.println("Distance to strafe x " + distanceToStrafe);
            ttStrafe.setByX(distanceToStrafe);

            // Get the angle from the camera to the AprilTag.
            double cameraAdjacent = Math.abs(cameraFaceY - aprilTagCenterY.get());
            double cameraOpposite = Math.abs(cameraFaceX - aprilTagCenterX.get());
            System.out.println("Camera adjacent " + cameraAdjacent + ", opposite " + cameraOpposite);

            double cameraHypotenuseSquared = Math.pow(cameraAdjacent, 2) + Math.pow(cameraOpposite, 2);
            double distanceFromCameraToAprilTag = Math.sqrt(cameraHypotenuseSquared);
            System.out.println("Distance from camera to AprilTag " + distanceFromCameraToAprilTag);

            double tanTheta = cameraOpposite / cameraAdjacent;
            double degreesFromCameraToAprilTag = Math.toDegrees(Math.atan(tanTheta));
            System.out.println("Degrees from camera to AprilTag " + degreesFromCameraToAprilTag);

            // Draw a line from the camera to the target AprilTag, the hypotenuse of the camera triangle.
            Line lineCH = new Line(cameraFaceX, cameraFaceY, aprilTagCenterX.get(), aprilTagCenterY.get());
            lineCH.setId("lineCH");
            lineCH.setStroke(Color.FUCHSIA);
            lineCH.getStrokeDashArray().addAll(10.0);
            lineCH.setStrokeWidth(3.0);
            pField.getChildren().add(lineCH);

            // Draw the opposite side of the camera triangle.
            Line lineCO = new Line(cameraFaceX, aprilTagCenterY.get(), aprilTagCenterX.get(), aprilTagCenterY.get());
            lineCO.setId("lineCO");
            lineCO.setStroke(Color.FUCHSIA);
            lineCO.getStrokeDashArray().addAll(10.0);
            lineCO.setStrokeWidth(3.0);
            pField.getChildren().add(lineCO);

            // Draw the adjacent side of the camera triangle.
            Line lineCA = new Line(cameraFaceX, cameraFaceY, cameraFaceX, aprilTagCenterY.get());
            lineCA.setId("lineCA");
            lineCA.setStroke(Color.FUCHSIA);
            lineCA.getStrokeDashArray().addAll(10.0);
            lineCA.setStrokeWidth(3.0);
            pField.getChildren().add(lineCA);

            // Get the angle and distance from the center of the robot to the AprilTag.
            // This is an intermediate step, but necessary in order to calculate the angle
            // by which the robot must be rotated so that the delivery device points at the
            // AprilTag and to calculate the final distance from the device to the AprilTag.
            // Set the sign of the angle from the camera to the AprilTag: for FTC negative
            // is clockwise.
            if (aprilTagCenterX.get() > cameraFaceX)
                degreesFromCameraToAprilTag *= -1;

            // The fields centerStageRobot.cameraCenterFromRobotCenterPX and
            // centerStageRobot.cameraOffsetFromRobotCenterPX are already signed correctly
            // for FTC.
            //**TODO Comment: need to include 1/2 of the height of the camera ...
            AngleDistance fromRobotCenter = CameraToCenterCorrections.getCorrectedAngleAndDistance2(degreesFromCameraToAprilTag,
                    distanceFromCameraToAprilTag,
                    centerStageRobot.cameraCenterFromRobotCenterPX + RobotFXCenterStageLG.CAMERA_HEIGHT / 2,
                    centerStageRobot.cameraOffsetFromRobotCenterPX);

            //AngleDistance fromRobotCenter = CameraToCenterCorrections.getCorrectedAngleAndDistance(centerStageRobot.cameraCenterFromRobotCenterPX,
            //        centerStageRobot.cameraOffsetFromRobotCenterPX, distanceFromCameraToAprilTag, degreesFromCameraToAprilTag);
            System.out.println("Angle from robot center to AprilTag " + fromRobotCenter.angle);
            System.out.println("Distance from robot center to AprilTag " + fromRobotCenter.distance);

            // sine of fromRobotCenter.angle = robotATOpposite / fromRobotCenter.distance;
            double robotATOpposite = Math.sin(Math.toRadians(Math.abs(fromRobotCenter.angle))) * fromRobotCenter.distance;
            // fromRobotCenter.distance squared = robotATOpposite squared + robotATAdjacent squared
            double robotATAdjacentSquared = Math.pow(fromRobotCenter.distance, 2) - Math.pow(robotATOpposite, 2);
            double robotATAdjacent = Math.sqrt(robotATAdjacentSquared);

            // We have the right triangle with a hypotenuse from the center of the robot to
            // the AprilTag. Now we need the right triangle with a hypotenuse from the center
            // of the robot to the point where a vertical line from the delivery device
            // intersects the horizontal line that intersects the centers of all 3 AprilTags.
            // The opposite side of the triangle is the same as the offset from the center of
            // the robot to the center of the device.
            double calculatedRobotCenterDeviceOpposite = robotCoordX.get() - deviceCenterX.get();
            double robotCenterDeviceOpposite = Math.abs(centerStageRobot.deviceOffsetFromRobotCenterPX);
            double robotDeviceHypotenuseSquared = Math.pow(robotATAdjacent, 2) + Math.pow(robotCenterDeviceOpposite, 2);
            double robotDeviceHypotenuse = Math.sqrt(robotDeviceHypotenuseSquared);

            // Get the angle at the center of the robot given the triangle defined above.
            double robotDeviceATSin = robotCenterDeviceOpposite / robotDeviceHypotenuse;
            double degreesFromRobotCenter = Math.toDegrees(Math.asin(robotDeviceATSin));
            System.out.println("Degrees from robot center to AprilTag horizontal opposite the device " + degreesFromRobotCenter);

            // Set the number of degrees to rotate so that the device is facing
            // the AprilTag.
            //**TODO Convert from FTC rotation to FX rotation.
            double finalDegreesFromRobotCenterToDevice = degreesFromRobotCenter;
            double finalDegreesFromRobotCenterToTarget = fromRobotCenter.angle;
            if (deviceCenterX.get() > aprilTagCenterX.get())
                finalDegreesFromRobotCenterToDevice *= -1;
            if (robotCoordX.get() < aprilTagCenterX.get())
                finalDegreesFromRobotCenterToTarget *= -1;

            rotateDeviceTowardsAprilTag.setByAngle(finalDegreesFromRobotCenterToDevice + finalDegreesFromRobotCenterToTarget);
        });

        PauseTransition pauseT = new PauseTransition(Duration.millis(2500));
        pauseT.setOnFinished(event -> {
            Line lineCHRef = (Line) pField.lookup("#lineCH");
            pField.getChildren().remove(lineCHRef);
            Line lineCORef = (Line) pField.lookup("#lineCO");
            pField.getChildren().remove(lineCORef);
            Line lineCARef = (Line) pField.lookup("#lineCA");
            pField.getChildren().remove(lineCARef);

            //**TODO Make this its own PauseTransition
            // Draw the triangle formed between the center of the robot and the delivery device.
            if (radioButtonText.equals("Turn to")) {
                Line lineRCDH = new Line(robotCoordX.get(), robotCoordY.get(), Math.abs(robotCoordX.get() - centerStageRobot.deviceOffsetFromRobotCenterPX), aprilTagCenterY.get());
                lineRCDH.setId("lineRCDH");
                lineRCDH.setStroke(Color.FUCHSIA);
                lineRCDH.getStrokeDashArray().addAll(10.0);
                lineRCDH.setStrokeWidth(3.0);
                pField.getChildren().add(lineRCDH);

                Line lineRCDO = new Line(robotCoordX.get(), aprilTagCenterY.get(), Math.abs(robotCoordX.get() - centerStageRobot.deviceOffsetFromRobotCenterPX), aprilTagCenterY.get());
                lineRCDO.setId("lineRCDO");
                lineRCDO.setStroke(Color.FUCHSIA);
                lineRCDO.getStrokeDashArray().addAll(10.0);
                lineRCDO.setStrokeWidth(3.0);
                pField.getChildren().add(lineRCDO);

                // Draw the adjacent side of the triangle. This side is shared between
                // two triangles.
                Line lineRCA = new Line(robotCoordX.get(), robotCoordY.get(), robotCoordX.get(), aprilTagCenterY.get());
                lineRCA.setId("lineRCA");
                lineRCA.setStroke(Color.FUCHSIA);
                lineRCA.getStrokeDashArray().addAll(10.0);
                lineRCA.setStrokeWidth(3.0);
                pField.getChildren().add(lineRCA);

                // Also show the hypotenuse of the right triangle formed between the center
                // of the robot and the AprilTag.
                Line lineRCAH = new Line(robotCoordX.get(), robotCoordY.get(), aprilTagCenterX.get(), aprilTagCenterY.get());
                lineRCAH.setId("lineRCAH");
                lineRCAH.setStroke(Color.AQUA);
                lineRCAH.getStrokeDashArray().addAll(10.0);
                lineRCAH.setStrokeWidth(3.0);
                pField.getChildren().add(lineRCAH);

                // One last thing: we need the distance from the delivery device
                // to the AprilTag. This is the hypotenuse of a right triangle.
                // double deviceHypotenuseSquared = deviceAdjacentSquared + deviceOppositeSquared
                double deviceAdjacent = deviceCenterY.get() - aprilTagCenterY.get();
                double deviceOpposite = Math.abs(deviceCenterX.get() - aprilTagCenterX.get());
                double deviceHypotenuseSquared = Math.pow(deviceAdjacent, 2) + Math.pow(deviceOpposite, 2);
                double deviceToAprilTag = Math.sqrt(deviceHypotenuseSquared);
                System.out.println("Distance from device to AprilTag " + deviceToAprilTag);
            }
        });

        PauseTransition preRotationPauseT = new PauseTransition(Duration.millis(2500));
        preRotationPauseT.setOnFinished(event -> {
            // Erase the lines
            Line lineRCDHRef = (Line) pField.lookup("#lineRCDH");
            pField.getChildren().remove(lineRCDHRef);
            Line lineRCDORef = (Line) pField.lookup("#lineRCDO");
            pField.getChildren().remove(lineRCDORef);
            Line lineRCARef = (Line) pField.lookup("#lineRCA");
            pField.getChildren().remove(lineRCARef);
            Line lineRCAHRef = (Line) pField.lookup("#lineRCAH");
            pField.getChildren().remove(lineRCAHRef);
        });

        // Look at the startup parameter that indicates whether to strafe or rotate.
        SequentialTransition seqTransition = new SequentialTransition(parallelT, pauseT); // common
        if (radioButtonText.equals("Strafe to"))
            seqTransition.getChildren().add(ttStrafe);
        else
            seqTransition.getChildren().addAll(preRotationPauseT, rotateDeviceTowardsAprilTag);

        //**TODO static method??
        new PlayPauseToggle(pPlayPauseButton, seqTransition);
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