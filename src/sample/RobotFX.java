package sample;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public abstract class RobotFX {

    // The size of the robot is 14" square = 356mm; at 1/6 scale = 60 pixels per side
    // By convention the width is the distance across the front of the robot.
    public static final double ROBOT_WIDTH_INCHES = 14.0;
    public static final double ROBOT_HEIGHT_INCHES = 14.0;
    public static final double ROBOT_WIDTH = 60; // pixels
    public static final double ROBOT_HEIGHT = 60;
    public static final double WHEEL_WIDTH = 10;
    public static final double WHEEL_HEIGHT = 20;
    public static final String ROBOT_GROUP_ID = "robotGroupId";
    public static final String ROBOT_BODY_ID = "robotBodyId";

    // The corners of the robot body.
    public enum Corners {LEFT_FRONT, RIGHT_FRONT, RIGHT_REAR, LEFT_REAR}

    protected final Group robot;
    private final Rectangle robotBody;

    public RobotFX(String pOpMode, Color pRobotBodyColor, Point2D pRobotBodyScreenCoordinates,
                   double pInitialHeading) {
        robot = new Group();
        robot.setId(pOpMode);

        robotBody = new Rectangle(pRobotBodyScreenCoordinates.getX(), pRobotBodyScreenCoordinates.getY(), ROBOT_WIDTH, ROBOT_HEIGHT);
        robotBody.setId(pOpMode + "_" + ROBOT_BODY_ID);
        robotBody.setArcHeight(15);
        robotBody.setArcWidth(15);
        robotBody.setStroke(Color.BLACK);
        robotBody.setFill(pRobotBodyColor);
        robot.getChildren().add(robotBody);

        Rectangle robotWheelLF = new Rectangle(pRobotBodyScreenCoordinates.getX() - WHEEL_WIDTH, pRobotBodyScreenCoordinates.getY() + 5, WHEEL_WIDTH, WHEEL_HEIGHT);
        robotWheelLF.setArcHeight(5);
        robotWheelLF.setArcWidth(5);
        robotWheelLF.setFill(Color.BLACK);
        robot.getChildren().add(robotWheelLF);

        Rectangle robotWheelRF = new Rectangle(pRobotBodyScreenCoordinates.getX() + ROBOT_WIDTH, pRobotBodyScreenCoordinates.getY() + 5, WHEEL_WIDTH, WHEEL_HEIGHT);
        robotWheelRF.setArcHeight(5);
        robotWheelRF.setArcWidth(5);
        robotWheelRF.setFill(Color.BLACK);
        robot.getChildren().add(robotWheelRF);

        Rectangle robotWheelLR = new Rectangle(pRobotBodyScreenCoordinates.getX() - 10, pRobotBodyScreenCoordinates.getY() + ROBOT_HEIGHT - WHEEL_HEIGHT - 5, WHEEL_WIDTH, WHEEL_HEIGHT);
        robotWheelLR.setArcHeight(5);
        robotWheelLR.setArcWidth(5);
        robotWheelLR.setFill(Color.BLACK);
        robot.getChildren().add(robotWheelLR);

        Rectangle robotWheelRR = new Rectangle(pRobotBodyScreenCoordinates.getX() + ROBOT_WIDTH, pRobotBodyScreenCoordinates.getY() + ROBOT_HEIGHT - WHEEL_HEIGHT - 5, WHEEL_WIDTH, WHEEL_HEIGHT);
        robotWheelRR.setArcHeight(5);
        robotWheelRR.setArcWidth(5);
        robotWheelRR.setFill(Color.BLACK);
        robot.getChildren().add(robotWheelRR);

        // Set the initial rotation of the robot.
        robot.setRotate(pInitialHeading);

        Point2D rbCoord = robotBody.localToScene(robotBody.getX(), robotBody.getY());
    }

    public Group getRobot() {
        return robot;
    }

    public Rectangle getRobotBody() {
        return robotBody;
    }
}
