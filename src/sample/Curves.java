package sample;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

// Combination of
// https://www.infoworld.com/article/2074529/javafx-2-animation--path-transitions.html
// and
// https://docs.oracle.com/javafx/2/animations/basics.htm#CJAJJAGI
public class Curves extends Application {

    public static final double ROBOT_WIDTH = 60; // pixels
    public static final double ROBOT_HEIGHT = 60;
    public static final double WHEEL_WIDTH = 10;
    public static final double WHEEL_HEIGHT = 20;
    public static final double PHONE_WIDTH = 20;
    public static final double PHONE_HEIGHT = 10;

    private enum Corners {TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT}

    @Override
    public void start(final Stage pStage) {

        FieldFX fieldFX = new FieldFX();
        Pane field = fieldFX.getField();

        pStage.setTitle("JavaFX CubicCurve");
        pStage.setScene(new Scene(field, FieldFX.FIELD_WIDTH, FieldFX.FIELD_HEIGHT, Color.GRAY));
        pStage.show();

        applyAnimation(field);
    }

    private void applyAnimation(Pane pField) {

        Group robot = new Group();
        final Rectangle robotBody = new Rectangle(120, 210, ROBOT_WIDTH, ROBOT_HEIGHT);
        robotBody.setFill(Color.CRIMSON);
        robotBody.setId("robotBodyId");
        robot.getChildren().add(robotBody);

        Rectangle robotPhone = new Rectangle(120 + (ROBOT_WIDTH / 2) - (PHONE_WIDTH / 2), 210, PHONE_WIDTH, PHONE_HEIGHT);
        robotPhone.setArcHeight(5);
        robotPhone.setArcWidth(5);
        robotPhone.setFill(Color.BLACK);
        robot.getChildren().add(robotPhone);

        // Clue from https://stackoverflow.com/questions/53302083/javafx-animate-along-path-parallel-to-tangent
        // Counteracts pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        // which places the robot perpendicular to the path.
        robot.getTransforms().add(new Rotate(90, 120 + (ROBOT_WIDTH / 2), 210 + (ROBOT_HEIGHT / 2)));
        pField.getChildren().add(robot);

        /*
        // Draw lines at 90 degrees.
        MoveTo mt = new MoveTo();
        mt.setX(150.0f);
        mt.setY(450.0f);

        VLineTo vlt = new VLineTo(150.0);
        HLineTo hlt = new HLineTo(300.0);

        final Path path = new Path();
        path.getElements().add(mt);
        path.getElements().add(vlt);
        path.getElements().add(hlt);
        pField.getChildren().add(path);

        // Draw a line at 45 degrees.
        MoveTo mt45 = new MoveTo();
        mt45.setX(150.0f);
        mt45.setY(150.0f);
        LineTo line45 = new LineTo(250.0, 50.0);

        final Path path45 = new Path();
        path.getElements().add(mt45);
        path.getElements().add(line45);
        pField.getChildren().add(path45);
         */

        SequentialTransition seqTrans = new SequentialTransition();

        final Path cPath1 = new Path();
        MoveTo moveTo1 = new MoveTo(150, 240);
        cPath1.getElements().add(moveTo1);

        // 90 degree curve; rotation after path transition is 359+; add 90 to get rotation of robot body
        CubicCurveTo cc90 = new CubicCurveTo(150, 149.5, 150, 149.5, 240, 150);
        cPath1.getElements().add(cc90);

        // 45 degree curve
        //CubicCurveTo cc45 = new CubicCurveTo(150, 149.5, 150, 149.5, 250, 50);
       //cPath1.getElements().add(cc45);

        //**TODO After the 90 degree turn move forward some distance then
        // make a 45 degree turn. Cannot start the 45 degree turn at the end of
        // the LineTo.

        // Line at 45 degrees; rotation after path transition is 45.0; add 90 to get the actual 135 degree
        // rotation of the robot body.
        //LineTo lineTo = new LineTo(340, 250);
        //cPath1.getElements().add(lineTo);

         //CubicCurveTo ccTo2 = new CubicCurveTo(382, 250, 382, 250, 300, 400);
        // CubicCurveTo ccTo2 = new CubicCurveTo(490, 236, 490, 236, 300, 400);
        //cPath1.getElements().add(ccTo2);

        pField.getChildren().add(cPath1);

        final PathTransition cpTrans1 = generatePathTransition(robot, cPath1);
        seqTrans.getChildren().add(cpTrans1);


        seqTrans.play();

        //translateAndRotate(group);

    }

    private Path generateCurvyPath() {

        Path path = new Path();
        MoveTo moveTo = new MoveTo(150, 240);
        path.getElements().add(moveTo);

        // 90 degree curve
        CubicCurveTo ccTo = new CubicCurveTo(150, 149.5, 150, 149.5, 240, 150);
        path.getElements().add(ccTo);

        // 45 degree curve
        //path.getElements().add(new CubicCurveTo(150, 149.5, 150, 149.5, 250, 50));
        return path;
    }


    private Path generateLineTo() {

        Path path = new Path();
        MoveTo moveTo = new MoveTo(150, 240);
        path.getElements().add(moveTo);

        // 90 degree curve
        LineTo lineTo = new LineTo(150, 150);
        path.getElements().add(lineTo);
        return path;
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

            Point2D centroid = computeRotatedCentroid(rbCoord.getX(), rbCoord.getY(), ROBOT_WIDTH, ROBOT_HEIGHT, robotRotation);
            System.out.println("Centroid x " + centroid.getX() + ", y " + centroid.getY());

            Map<Corners, Point2D> cornerMap = robotBodyCornerCoordinates(centroid.getX(), centroid.getY(), ROBOT_WIDTH, ROBOT_HEIGHT, robotRotation);
            System.out.println("Top left x " + cornerMap.get(Corners.TOP_LEFT).getX() + " y " + cornerMap.get(Corners.TOP_LEFT).getY());
            System.out.println("Top right x " + cornerMap.get(Corners.TOP_RIGHT).getX() + " y " + cornerMap.get(Corners.TOP_RIGHT).getY());
            System.out.println("Bottom right x " + cornerMap.get(Corners.BOTTOM_RIGHT).getX() + " y " + cornerMap.get(Corners.BOTTOM_RIGHT).getY());
            System.out.println("Bottom left x " + cornerMap.get(Corners.BOTTOM_LEFT).getX() + " y " + cornerMap.get(Corners.BOTTOM_LEFT).getY());

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