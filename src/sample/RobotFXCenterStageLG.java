package sample;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class RobotFXCenterStageLG extends RobotFXLG {

    public static final double CAMERA_WIDTH = FieldFXCenterStageBackdropLG.PX_PER_INCH * 3;
    public static final double CAMERA_HEIGHT = FieldFXCenterStageBackdropLG.PX_PER_INCH * 2;
    public static final String CAMERA_ON_ROBOT_ID = "cameraOnRobotId";
    public static final String DEVICE_ON_ROBOT_ID = "deviceOnRobotId";

    public RobotFXCenterStageLG(double pRobotBodyWidth, double pRobotBodyHeight, Color pRobotBodyColor,
                                Point2D pRobotScreenCoordinates, double pInitialHeading) {
        super(pRobotBodyWidth, pRobotBodyHeight, pRobotBodyColor, pRobotScreenCoordinates, pInitialHeading);

        // Place the camera on the robot.
        Rectangle cameraOnRobot = new Rectangle(pRobotScreenCoordinates.getX() + WHEEL_WIDTH + (pRobotBodyWidth * FieldFXCenterStageBackdropLG.PX_PER_INCH) - (CAMERA_WIDTH + FieldFXCenterStageBackdropLG.PX_PER_INCH), pRobotScreenCoordinates.getY() + FieldFXCenterStageBackdropLG.PX_PER_INCH, CAMERA_WIDTH, CAMERA_HEIGHT);
        cameraOnRobot.setId(robot.getId() + "_" + CAMERA_ON_ROBOT_ID);
        cameraOnRobot.setArcHeight(5);
        cameraOnRobot.setArcWidth(5);
        cameraOnRobot.setFill(Color.BLACK);
        robot.getChildren().add(cameraOnRobot);

        // Show a device mounted on the robot. The device may be a shooter, a delivery arm,
        // a grabber, etc.
        Circle deviceOnRobot = new Circle(pRobotScreenCoordinates.getX() + WHEEL_WIDTH + FieldFXCenterStageBackdropLG.PX_PER_INCH * 2, pRobotScreenCoordinates.getY() + FieldFXCenterStageBackdropLG.PX_PER_INCH * 2, FieldFXCenterStageBackdropLG.PX_PER_INCH);
        deviceOnRobot.setId(robot.getId() + "_" + DEVICE_ON_ROBOT_ID);
        deviceOnRobot.setStrokeWidth(2.0);
        deviceOnRobot.setStroke(Color.YELLOW);
        deviceOnRobot.setFill(Color.TRANSPARENT);
        robot.getChildren().add(deviceOnRobot);
    }

}
