package sample;

public class RobotConstants {

    public static final String IMAGE_DIR = "/images/";
    public static final String LOG_DIR = "/logs/";
    public static final String XML_DIR = "/xml/";
    public static final String imageFilePrefix = "Image_";

    public enum RunType {
        AUTONOMOUS, TELEOP
    }

    public enum Alliance {
        BLUE, RED, NONE
    }

    public enum RecognitionResults {
        RECOGNITION_INTERNAL_ERROR, RECOGNITION_SUCCESSFUL, RECOGNITION_UNSUCCESSFUL
    }

}
