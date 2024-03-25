package sample;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public abstract class RobotFXLG {

    protected final double robotWidth;
    protected final double robotHeight;
    public static final double WHEEL_WIDTH = FieldFXCenterStageBackdropLG.PX_PER_INCH * 2;
    public static final double WHEEL_HEIGHT = FieldFXCenterStageBackdropLG.PX_PER_INCH * 4;
    public static final double WHEEL_OFFSET = FieldFXCenterStageBackdropLG.PX_PER_INCH * 1;
    public static final String ROBOT_ID = "robotGroup";

    protected final Group robot;

    // Place the wheels "under" the robot by drawing their black
    // rectangles over the body.
    public RobotFXLG(double pRobotWidth, double pRobotHeight, Color pRobotBodyColor,
                     Point2D pRobotScreenCoordinates, double pInitialHeading) {
        robotWidth = pRobotWidth;
        robotHeight = pRobotHeight;
        robot = new Group();
        robot.setId(ROBOT_ID);

        Rectangle robotBody = new Rectangle(pRobotScreenCoordinates.getX(), pRobotScreenCoordinates.getY(), robotWidth, robotHeight);
        robotBody.setArcHeight(15);
        robotBody.setArcWidth(15);
        robotBody.setStroke(Color.BLACK);
        robotBody.setFill(pRobotBodyColor);
        robot.getChildren().add(robotBody);

        Rectangle robotWheelLF = new Rectangle(pRobotScreenCoordinates.getX() + WHEEL_OFFSET, pRobotScreenCoordinates.getY() + WHEEL_OFFSET, WHEEL_WIDTH, WHEEL_HEIGHT);
        robotWheelLF.setArcHeight(5);
        robotWheelLF.setArcWidth(5);
        robotWheelLF.setFill(Color.BLACK);
        robot.getChildren().add(robotWheelLF);

        Rectangle robotWheelRF = new Rectangle(pRobotScreenCoordinates.getX() + robotWidth - (WHEEL_WIDTH + WHEEL_OFFSET), pRobotScreenCoordinates.getY() + WHEEL_OFFSET, WHEEL_WIDTH, WHEEL_HEIGHT);
        robotWheelRF.setArcHeight(5);
        robotWheelRF.setArcWidth(5);
        robotWheelRF.setFill(Color.BLACK);
        robot.getChildren().add(robotWheelRF);

        Rectangle robotWheelLR = new Rectangle(pRobotScreenCoordinates.getX() + WHEEL_OFFSET, pRobotScreenCoordinates.getY() + robotHeight - (WHEEL_HEIGHT + WHEEL_OFFSET), WHEEL_WIDTH, WHEEL_HEIGHT);
        robotWheelLR.setArcHeight(5);
        robotWheelLR.setArcWidth(5);
        robotWheelLR.setFill(Color.BLACK);
        robot.getChildren().add(robotWheelLR);

        Rectangle robotWheelRR = new Rectangle(pRobotScreenCoordinates.getX() + robotWidth - (WHEEL_WIDTH + WHEEL_OFFSET), pRobotScreenCoordinates.getY() + robotHeight - (WHEEL_HEIGHT + WHEEL_OFFSET), WHEEL_WIDTH, WHEEL_HEIGHT);
        robotWheelRR.setArcHeight(5);
        robotWheelRR.setArcWidth(5);
        robotWheelRR.setFill(Color.BLACK);
        robot.getChildren().add(robotWheelRR);

        // Set the initial rotation of the robot.
        robot.setRotate(pInitialHeading);
    }

    public Group getRobot() {
        return robot;
    }

    public double getRobotWidth() {
        return robotWidth;
    }

    public double getRobotHeight() {
        return robotHeight;
    }

}
