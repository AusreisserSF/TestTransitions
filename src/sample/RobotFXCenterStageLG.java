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

    //**TODO Convert all measurements from inches to pixels here.
    public RobotFXCenterStageLG(double pRobotWidth, double pRobotHeight, Color pRobotBodyColor,
                                double pCameraCenterFromRobotCenter, double pCameraOffsetFromRobotCenter,
                                double pDeviceCenterFromRobotCenter, double pDeviceOffsetFromRobotCenter,
                                Point2D pRobotScreenCoordinates, double pInitialHeading) {
        super(pRobotWidth * FieldFXCenterStageBackdropLG.PX_PER_INCH,
                pRobotHeight * FieldFXCenterStageBackdropLG.PX_PER_INCH,
                pRobotBodyColor, pRobotScreenCoordinates, pInitialHeading);

        //**TODO Need local variables *in pixels* for
//      // double pCameraCenterFromRobotCenter * FieldFXCenterStageBackdropLG.PX_PER_INCH,
//      double pCameraOffsetFromRobotCenter * FieldFXCenterStageBackdropLG.PX_PER_INCH,
//      // double pDeviceCenterFromRobotCenter * FieldFXCenterStageBackdropLG.PX_PER_INCH,
//      double pDeviceOffsetFromRobotCenter * FieldFXCenterStageBackdropLG.PX_PER_INCH,

        // Place the camera on the robot.
        double cameraYBaseline = (robotHeight / 2) - (CAMERA_HEIGHT / 2);
        // If the parameter is < 0 then the camera center is *below* robot
        // center so subtract the negative parameter. If the parameter is >
        // 0 then the camera is *above* robot center so subtract the positive
        // number.
        double cameraYFinal = cameraYBaseline - pCameraCenterFromRobotCenter;

        double cameraXBaseline = (robotWidth / 2) - (CAMERA_WIDTH / 2);
        // If the parameter is < 0 then the camera center is to the *right*
        // of robot center so subtract the negative parameter. If the parameter
        // is > 0 then the camera is to the *left* of robot center so subtract
        // the positive number.
        double cameraXFinal = cameraXBaseline - pCameraCenterFromRobotCenter;

        Rectangle cameraOnRobot = new Rectangle(pRobotScreenCoordinates.getX() + cameraXFinal,
                pRobotScreenCoordinates.getY() + cameraYFinal, CAMERA_WIDTH, CAMERA_HEIGHT);
        cameraOnRobot.setId(robot.getId() + "_" + CAMERA_ON_ROBOT_ID);
        cameraOnRobot.setArcHeight(5);
        cameraOnRobot.setArcWidth(5);
        cameraOnRobot.setFill(Color.YELLOW);
        robot.getChildren().add(cameraOnRobot);

        // Show a device mounted on the robot. The device may be a shooter, a delivery arm,
        // a grabber, etc.
        Circle deviceOnRobot = new Circle(pRobotScreenCoordinates.getX() + FieldFXCenterStageBackdropLG.PX_PER_INCH * 2, pRobotScreenCoordinates.getY() + FieldFXCenterStageBackdropLG.PX_PER_INCH * 2, FieldFXCenterStageBackdropLG.PX_PER_INCH);
        deviceOnRobot.setId(robot.getId() + "_" + DEVICE_ON_ROBOT_ID);
        deviceOnRobot.setStrokeWidth(2.0);
        deviceOnRobot.setStroke(Color.YELLOW);
        deviceOnRobot.setFill(Color.TRANSPARENT);
        robot.getChildren().add(deviceOnRobot);
    }

}
