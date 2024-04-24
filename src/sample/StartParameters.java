package sample;

public class StartParameters {

    public enum ApproachBackdrop { STRAFE_TO, TURN_TO }

    public final double robotWidth;
    public final double robotHeight;
    public final double cameraCenterFromRobotCenter;
    public final double cameraOffsetFromRobotCenter;
    public final double deviceCenterFromRobotCenter;
    public final double deviceOffsetFromRobotCenter;
    public final double robotPositionAtBackdropX;
    public final double robotPositionAtBackdropY;
    public final int aprilTagId;
    public final ApproachBackdrop approachBackdrop;

    public StartParameters(double pRobotWidth, double pRobotHeight,
                           double pCameraCenterFromRobotCenter, double pCameraOffsetFromRobotCenter,
                           double pDeviceCenterFromRobotCenter, double pDeviceOffsetFromRobotCenter,
                           double pRobotPositionAtBackdropX, double pRobotPositionAtBackdropY,
                           int pAprilTagId, ApproachBackdrop pApproachBackdrop) {
        robotWidth = pRobotWidth;
        robotHeight = pRobotHeight;
        cameraCenterFromRobotCenter = pCameraCenterFromRobotCenter;
        cameraOffsetFromRobotCenter = pCameraOffsetFromRobotCenter;
        deviceCenterFromRobotCenter = pDeviceCenterFromRobotCenter;
        deviceOffsetFromRobotCenter = pDeviceOffsetFromRobotCenter;
        robotPositionAtBackdropX = pRobotPositionAtBackdropX;
        robotPositionAtBackdropY = pRobotPositionAtBackdropY;
        aprilTagId = pAprilTagId;
        approachBackdrop = pApproachBackdrop;
    }

}