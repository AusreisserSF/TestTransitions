package sample;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public abstract class RobotFXLG {

    // The size of the robot is 14" square = 356mm; at 1/6 scale = 60 pixels per side
    // By convention the width is the distance across the front of the robot.

    public static final double ROBOT_BODY_WIDTH = FieldFXCenterStageBackdropLG.PX_PER_INCH * 14.0; // pixels
    public static final double ROBOT_BODY_HEIGHT = FieldFXCenterStageBackdropLG.PX_PER_INCH * 14.0;
    public static final double WHEEL_WIDTH = FieldFXCenterStageBackdropLG.PX_PER_INCH * 2;
    public static final double WHEEL_HEIGHT = FieldFXCenterStageBackdropLG.PX_PER_INCH * 4;
    public static final double WHEEL_OFFSET_Y = FieldFXCenterStageBackdropLG.PX_PER_INCH * 1;
    public static final String ROBOT_GROUP_ID = "robotGroupId";

    protected final Group robot;

    public RobotFXLG(Point2D pRobotScreenCoordinates,
                     double pInitialHeading, Color pRobotBodyColor) {
        robot = new Group();

        Rectangle robotBody = new Rectangle(pRobotScreenCoordinates.getX() + WHEEL_WIDTH, pRobotScreenCoordinates.getY(), ROBOT_BODY_WIDTH, ROBOT_BODY_HEIGHT);
        robotBody.setArcHeight(15);
        robotBody.setArcWidth(15);
        robotBody.setStroke(Color.BLACK);
        robotBody.setFill(pRobotBodyColor);
        robot.getChildren().add(robotBody);

        Rectangle robotWheelLF = new Rectangle(pRobotScreenCoordinates.getX(), pRobotScreenCoordinates.getY() + WHEEL_OFFSET_Y, WHEEL_WIDTH, WHEEL_HEIGHT);
        robotWheelLF.setArcHeight(5);
        robotWheelLF.setArcWidth(5);
        robotWheelLF.setFill(Color.BLACK);
        robot.getChildren().add(robotWheelLF);

        Rectangle robotWheelRF = new Rectangle(pRobotScreenCoordinates.getX() + ROBOT_BODY_WIDTH + WHEEL_WIDTH, pRobotScreenCoordinates.getY() + WHEEL_OFFSET_Y, WHEEL_WIDTH, WHEEL_HEIGHT);
        robotWheelRF.setArcHeight(5);
        robotWheelRF.setArcWidth(5);
        robotWheelRF.setFill(Color.BLACK);
        robot.getChildren().add(robotWheelRF);

        Rectangle robotWheelLR = new Rectangle(pRobotScreenCoordinates.getX(), pRobotScreenCoordinates.getY() + ROBOT_BODY_HEIGHT - WHEEL_HEIGHT - WHEEL_OFFSET_Y, WHEEL_WIDTH, WHEEL_HEIGHT);
        robotWheelLR.setArcHeight(5);
        robotWheelLR.setArcWidth(5);
        robotWheelLR.setFill(Color.BLACK);
        robot.getChildren().add(robotWheelLR);

        Rectangle robotWheelRR = new Rectangle(pRobotScreenCoordinates.getX() + ROBOT_BODY_WIDTH + WHEEL_WIDTH, pRobotScreenCoordinates.getY() + ROBOT_BODY_HEIGHT - WHEEL_HEIGHT - WHEEL_OFFSET_Y, WHEEL_WIDTH, WHEEL_HEIGHT);
        robotWheelRR.setArcHeight(5);
        robotWheelRR.setArcWidth(5);
        robotWheelRR.setFill(Color.BLACK);
        robot.getChildren().add(robotWheelRR);

        //**TODO This really doesn't work because the rotated robot ends up
        // too far awa from the wall and the wheels cross the line.
        // Set the initial rotation of the robot.
        robot.setRotate(pInitialHeading);
    }

    public Group getRobot() {
        return robot;
    }

}
