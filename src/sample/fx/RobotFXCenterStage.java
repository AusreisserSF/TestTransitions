package sample.fx;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class RobotFXCenterStage extends RobotFX {

    public static final double CAMERA_WIDTH = 20;
    public static final double CAMERA_HEIGHT = 7;
    public static final String DEVICE_ON_ROBOT_ID = "deviceOnRobotId";

    public RobotFXCenterStage(String pOpMode, Color pRobotBodyColor,
                               Point2D pRobotBodyScreenCoordinates,
                               double pInitialHeading) {
        super(pOpMode, pRobotBodyColor, pRobotBodyScreenCoordinates, pInitialHeading);

        // Place the camera on the robot.
        Rectangle robotCamera = new Rectangle(pRobotBodyScreenCoordinates.getX() + (ROBOT_WIDTH - CAMERA_WIDTH) - 4, pRobotBodyScreenCoordinates.getY() + 4, CAMERA_WIDTH, CAMERA_HEIGHT);
        robotCamera.setArcHeight(5);
        robotCamera.setArcWidth(5);
        robotCamera.setFill(Color.BLACK);
        robot.getChildren().add(robotCamera);

        // Show a device mounted on the robot. The device may be a shooter, a delivery arm,
        // a grabber, etc.
        Circle deviceOnRobot = new Circle(pRobotBodyScreenCoordinates.getX() + 8, pRobotBodyScreenCoordinates.getY() + 8, 6);
        deviceOnRobot.setId(robot.getId() + "_" + DEVICE_ON_ROBOT_ID);
        deviceOnRobot.setFill(Color.YELLOW);
        robot.getChildren().add(deviceOnRobot);
    }

}
