package sample.auto.xml;

public class StartParameters {
    
    public final String robotWidth;
    public final String robotHeight;
    public final String cameraCenterFromRobotCenter;
    public final String cameraOffsetFromRobotCenter;
    public final String cameraFieldOfView;
    public final String deviceCenterFromRobotCenter;
    public final String deviceOffsetFromRobotCenter;
    public final String robotPositionAtBackdropX;
    public final String robotPositionAtBackdropY;

    public StartParameters(String pRobotWidth, String pRobotHeight,
                           String pCameraCenterFromRobotCenter, String pCameraOffsetFromRobotCenter,
                           String pCameraFieldOfView,
                           String pDeviceCenterFromRobotCenter, String pDeviceOffsetFromRobotCenter,
                           String pRobotPositionAtBackdropX, String pRobotPositionAtBackdropY) {
        robotWidth = pRobotWidth;
        robotHeight = pRobotHeight;
        cameraCenterFromRobotCenter = pCameraCenterFromRobotCenter;
        cameraOffsetFromRobotCenter = pCameraOffsetFromRobotCenter;
        cameraFieldOfView = pCameraFieldOfView;
        deviceCenterFromRobotCenter = pDeviceCenterFromRobotCenter;
        deviceOffsetFromRobotCenter = pDeviceOffsetFromRobotCenter;
        robotPositionAtBackdropX = pRobotPositionAtBackdropX;
        robotPositionAtBackdropY = pRobotPositionAtBackdropY;
    }

}