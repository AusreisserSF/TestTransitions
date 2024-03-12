package sample;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class RobotFXCenterStageLG extends RobotFXLG {

    public static final double CAMERA_WIDTH = FieldFXCenterStageBackdropLG.PX_PER_INCH * 3;
    public static final double CAMERA_HEIGHT = FieldFXCenterStageBackdropLG.PX_PER_INCH * 2;
    public static final String DEVICE_ON_ROBOT_ID = "deviceOnRobotId";

    public RobotFXCenterStageLG(String pOpMode, Color pRobotBodyColor,
                                Point2D pRobotBodyScreenCoordinates,
                                double pInitialHeading) {
        super(pOpMode, pRobotBodyColor, pRobotBodyScreenCoordinates, pInitialHeading);

        // Place the camera on the robot.
        Rectangle robotCamera = new Rectangle(pRobotBodyScreenCoordinates.getX() + RobotFXLG.ROBOT_WIDTH - (CAMERA_WIDTH + FieldFXCenterStageBackdropLG.PX_PER_INCH), pRobotBodyScreenCoordinates.getY() + FieldFXCenterStageBackdropLG.PX_PER_INCH, CAMERA_WIDTH, CAMERA_HEIGHT);
        robotCamera.setArcHeight(5);
        robotCamera.setArcWidth(5);
        robotCamera.setFill(Color.BLACK);
        robot.getChildren().add(robotCamera);

        // Show a device mounted on the robot. The device may be a shooter, a delivery arm,
        // a grabber, etc.
        Circle deviceOnRobot = new Circle(pRobotBodyScreenCoordinates.getX() + FieldFXCenterStageBackdropLG.PX_PER_INCH * 2, pRobotBodyScreenCoordinates.getY() + FieldFXCenterStageBackdropLG.PX_PER_INCH * 2, FieldFXCenterStageBackdropLG.PX_PER_INCH);
        deviceOnRobot.setId(robot.getId() + "_" + DEVICE_ON_ROBOT_ID);
        deviceOnRobot.setFill(Color.YELLOW);
        robot.getChildren().add(deviceOnRobot);
    }

}
