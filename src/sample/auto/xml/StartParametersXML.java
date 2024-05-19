package sample.auto.xml;

import org.firstinspires.ftc.ftcdevcommon.AutonomousRobotException;
import org.firstinspires.ftc.ftcdevcommon.platform.intellij.RobotLogCommon;
import org.firstinspires.ftc.ftcdevcommon.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import sample.auto.RobotConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class StartParametersXML {

    public static final String TAG = StartParametersXML.class.getSimpleName();
    private static final String FILE_NAME = "StartParameters.xml";
    private final String xmlDirectory;
    private final String xmlFilePath;
    private final Document document;

    // References to DOM nodes whose contents we might want
    // to change later.
    private Node robot_width_node;
    private Node robot_height_node;
    private Node camera_center_node;
    private Node camera_offset_node;
    private Node camera_fov_node;
    private Node device_center_node;
    private Node device_offset_node;
    private Node backdrop_x_node;
    private Node backdrop_y_node;

    private final StartParameters startParameters;

    // IntelliJ only - if you want a validating parser.
    // private static final String JAXP_SCHEMA_LANGUAGE =
    // "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    // private static final String W3C_XML_SCHEMA =
    // "http://www.w3.org/2001/XMLSchema";
    // End IntelliJ only

    // Read start parameters from from an XML file but do not include those that are more easily
    // changed via the UI: the AprilTag target (Spinner) and the RadioButton/ToggleGroup for the
    // way the robot should approach the target - strafe or turn.
    public StartParametersXML(String pXMLDirectory) throws ParserConfigurationException, SAXException, IOException {

        // IntelliJ only
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setIgnoringComments(true);
        dbFactory.setNamespaceAware(true);
        //dbFactory.setValidating(true);
        //dbFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

        // ## ONLY works with a validating parser.
        //dbFactory.setIgnoringElementContentWhitespace(true);
        // End IntelliJ only

        xmlDirectory = pXMLDirectory;
        xmlFilePath = xmlDirectory + FILE_NAME;

        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        document = dBuilder.parse(new File(xmlFilePath));
        Element startParametersRoot = document.getDocumentElement();

        // <robot_width>
        robot_width_node = startParametersRoot.getFirstChild();
        robot_width_node = XMLUtils.getNextElement(robot_width_node);
        if (robot_width_node == null || !robot_width_node.getNodeName().equals("robot_width")
                || robot_width_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'robot_width' not found");

        String robotWidth = robot_width_node.getTextContent();
        validateDoubleContent(robotWidth, "robot_width");

        // <robot_height>
        robot_height_node = robot_width_node.getNextSibling();
        robot_height_node = XMLUtils.getNextElement(robot_height_node);
        if (robot_height_node == null || !robot_height_node.getNodeName().equals("robot_height")
                || robot_height_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'robot_height' not found");

        String robotHeight = robot_height_node.getTextContent();
        validateDoubleContent(robotHeight, "robot_height");

        // <camera_center_from_robot_center>
        camera_center_node = robot_height_node.getNextSibling();
        camera_center_node = XMLUtils.getNextElement(camera_center_node);
        if (camera_center_node == null || !camera_center_node.getNodeName().equals("camera_center_from_robot_center")
                || camera_center_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'camera_center_from_robot_center' not found");

        String cameraCenter = camera_center_node.getTextContent();
        validateDoubleContent(cameraCenter, "camera_center_from_robot_center");

        // <camera_offset_from_robot_center>
        camera_offset_node = camera_center_node.getNextSibling();
        camera_offset_node = XMLUtils.getNextElement(camera_offset_node);
        if (camera_offset_node == null || !camera_offset_node.getNodeName().equals("camera_offset_from_robot_center")
                || camera_center_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'camera_offset_from_robot_center' not found");

        String cameraOffset = camera_offset_node.getTextContent();
        validateDoubleContent(cameraOffset, "camera_offset_from_robot_center");

        // <camera_field_of_view>
        camera_fov_node = camera_offset_node.getNextSibling();
        camera_fov_node = XMLUtils.getNextElement(camera_fov_node);
        if (camera_fov_node == null || !camera_fov_node.getNodeName().equals("camera_field_of_view")
                || camera_fov_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'camera_field_of_view' not found");

        String cameraFOV = camera_fov_node.getTextContent();
        validateDoubleContent(cameraFOV, "camera_field_of_view");

        // <device_center_from_robot_center>
        device_center_node = camera_fov_node.getNextSibling();
        device_center_node = XMLUtils.getNextElement(device_center_node);
        if (device_center_node == null || !device_center_node.getNodeName().equals("device_center_from_robot_center")
                || camera_center_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'device_center_from_robot_center' not found");

        String deviceCenter = device_center_node.getTextContent();
        validateDoubleContent(deviceCenter, "device_center_from_robot_center");

        // <device_offset_from_robot_center>
        device_offset_node = device_center_node.getNextSibling();
        device_offset_node = XMLUtils.getNextElement(device_offset_node);
        if (device_offset_node == null || !device_offset_node.getNodeName().equals("device_offset_from_robot_center")
                || camera_center_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'device_offset_from_robot_center' not found");

        String deviceOffset = device_offset_node.getTextContent();
        validateDoubleContent(deviceOffset, "device_offset_from_robot_center");

        // <robot_position_at_backdrop_x>
        backdrop_x_node = device_offset_node.getNextSibling();
        backdrop_x_node = XMLUtils.getNextElement(backdrop_x_node);
        if (backdrop_x_node == null || !backdrop_x_node.getNodeName().equals("robot_position_at_backdrop_x")
                || camera_center_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'robot_position_at_backdrop_x' not found");

        String backdropX = backdrop_x_node.getTextContent();
        validateDoubleContent(backdropX, "robot_position_at_backdrop_x");

        // <robot_position_at_backdrop_y>
        backdrop_y_node = backdrop_x_node.getNextSibling();
        backdrop_y_node = XMLUtils.getNextElement(backdrop_y_node);
        if (backdrop_y_node == null || !backdrop_y_node.getNodeName().equals("robot_position_at_backdrop_y")
                || camera_center_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'robot_position_at_backdrop_y' not found");

        String backdropY = backdrop_y_node.getTextContent();
        validateDoubleContent(backdropY, "robot_position_at_backdrop_y");

        startParameters = new StartParameters(robotWidth, robotHeight, cameraCenter, cameraOffset, cameraFOV,
                deviceCenter, deviceOffset, backdropX, backdropY);

        RobotLogCommon.c(TAG, "In StartParametersXML; opened and parsed the XML file");
    }

    public StartParameters getStartParameters() {
        return startParameters;
    }

    // Replaces the text value of the <robot_width> element.
    public void setRobotWidth(double pRobotWidth) {
        robot_width_node.setTextContent(Double.toString(pRobotWidth));
    }

    // Replaces the text value of the <robot_height> element.
    public void setRobotHeight(double pRobotHeight) {
        robot_height_node.setTextContent(Double.toString(pRobotHeight));
    }

    // Replaces the text value of the <camera_center_from_robot_center> element.
    public void setCameraCenterFromRobotCenter(double pCameraCenterFromRobotCenter) {
        camera_center_node.setTextContent(Double.toString(pCameraCenterFromRobotCenter));
    }

    // Replaces the text value of the <camera_offset_from_robot_center> element.
    public void setCameraOffsetFromRobotCenter(double pCameraOffsetFromRobotCenter) {
        camera_offset_node.setTextContent(Double.toString(pCameraOffsetFromRobotCenter));
    }

    // Replaces the text value of the <camera_field_of_view> element.
    public void setCameraFOV(double pCameraFOV) {
        camera_fov_node.setTextContent(Double.toString(pCameraFOV));
    }

    // Replaces the text value of the <device_center_from_robot_center> element.
    public void setDeviceCenterFromRobotCenter(double pDeviceCenterFromRobotCenter) {
        device_center_node.setTextContent(Double.toString(pDeviceCenterFromRobotCenter));
    }

    // Replaces the text value of the <device_offset_from_robot_center> element.
    public void setDeviceOffsetFromRobotCenter(double pDeviceOffsetFromRobotCenter) {
        device_offset_node.setTextContent(Double.toString(pDeviceOffsetFromRobotCenter));
    }

    // Replaces the text value of the <robot_position_at_backdrop_x> element.
    public void setRobotPositionAtBackdropX(double pRobotPositionAtBackdropX) {
        backdrop_x_node.setTextContent(Double.toString(pRobotPositionAtBackdropX));
    }

    // Replaces the text value of the <robot_position_at_backdrop_y> element.
    public void setRobotPositionAtBackdropY(double pRobotPositionAtBackdropY) {
        backdrop_y_node.setTextContent(Double.toString(pRobotPositionAtBackdropY));
    }

    public void writeStartParametersFile() {
        XMLUtils.writeXMLFile(document, xmlFilePath, xmlDirectory + RobotConstants.XSLT_FILE_NAME);
    }

    private void validateDoubleContent(String pElementText, String pElementName) {
        try {
            Double.parseDouble(pElementText);
        } catch (NumberFormatException nex) {
            throw new AutonomousRobotException(TAG, "Invalid number format in element '" + pElementName + "'");
        }
    }

}