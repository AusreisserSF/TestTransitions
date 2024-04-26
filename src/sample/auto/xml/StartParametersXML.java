package sample.auto.xml;

import org.firstinspires.ftc.ftcdevcommon.AutonomousRobotException;
import org.firstinspires.ftc.ftcdevcommon.platform.intellij.RobotLogCommon;
import org.firstinspires.ftc.ftcdevcommon.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

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

    private final StartParameters startParameters;

    // IntelliJ only
    /*
     * private static final String JAXP_SCHEMA_LANGUAGE =
     * "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
     * private static final String W3C_XML_SCHEMA =
     * "http://www.w3.org/2001/XMLSchema";
     */
    // End IntelliJ only

    //**TODO The values in the XML file are meant to override the values in the fxml file.
    // But the XML values should be validated against the rules set up in StartParameterValidation,
    // which are currently triggered via a ChangeListener ... Need to validate the XML against
    // the ranges in StartParameterValidation, e.g. robot width, and if they are out of range
    // mark them as invalid, i.e. requiring change, during initialization.
    public StartParametersXML(String pXMLDirectory) throws ParserConfigurationException, SAXException, IOException {

        // IntelliJ only
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setIgnoringComments(true);
        dbFactory.setNamespaceAware(true);
        dbFactory.setValidating(true);
        //dbFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

        // ## ONLY works with a validating parser.
        dbFactory.setIgnoringElementContentWhitespace(true);
        // End IntelliJ only

        xmlDirectory = pXMLDirectory;
        xmlFilePath = xmlDirectory + FILE_NAME;

        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        document = dBuilder.parse(new File(xmlFilePath));
        Element startParametersRoot = document.getDocumentElement();

        // <robot_width>
        Node robot_width_node = startParametersRoot.getFirstChild();
        robot_width_node = XMLUtils.getNextElement(robot_width_node);
        if (robot_width_node == null || !robot_width_node.getNodeName().equals("robot_width")
                || robot_width_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'robot_width' not found");

        double robotWidth = getDoubleContent(robot_width_node.getTextContent(), "robot_width");

        // <robot_height>
        Node robot_height_node = robot_width_node.getNextSibling();
        robot_height_node = XMLUtils.getNextElement(robot_height_node);
        if (robot_height_node == null || !robot_height_node.getNodeName().equals("robot_height")
                || robot_height_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'robot_height' not found");

        double robotHeight = getDoubleContent(robot_height_node.getTextContent(), "robot_height");

        // <camera_center_from_robot_center>
        Node camera_center_node = robot_height_node.getNextSibling();
        camera_center_node = XMLUtils.getNextElement(camera_center_node);
        if (camera_center_node == null || !camera_center_node.getNodeName().equals("camera_center_from_robot_center")
                || camera_center_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'camera_center_from_robot_center' not found");

        double cameraCenter = getDoubleContent(camera_center_node.getTextContent(), "camera_center_from_robot_center");

        // <camera_offset_from_robot_center>
        Node camera_offset_node = camera_center_node.getNextSibling();
        camera_offset_node = XMLUtils.getNextElement(camera_offset_node);
        if (camera_offset_node == null || !camera_offset_node.getNodeName().equals("camera_offset_from_robot_center")
                || camera_center_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'camera_offset_from_robot_center' not found");

        double cameraOffset = getDoubleContent(camera_offset_node.getTextContent(), "camera_offset_from_robot_center");

        // <device_center_from_robot_center>
        Node device_center_node = camera_center_node.getNextSibling();
        device_center_node = XMLUtils.getNextElement(device_center_node);
        if (device_center_node == null || !device_center_node.getNodeName().equals("device_center_from_robot_center")
                || camera_center_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'device_center_from_robot_center' not found");

        double deviceCenter = getDoubleContent(device_center_node.getTextContent(), "device_center_from_robot_center");

        // <device_offset_from_robot_center>
        Node device_offset_node = device_center_node.getNextSibling();
        device_offset_node = XMLUtils.getNextElement(device_offset_node);
        if (device_offset_node == null || !device_offset_node.getNodeName().equals("device_offset_from_robot_center")
                || camera_center_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'device_offset_from_robot_center' not found");

        double deviceOffset = getDoubleContent(device_offset_node.getTextContent(), "device_offset_from_robot_center");

        // <robot_position_at_backdrop_x>36.0</robot_position_at_backdrop_x>
        Node backdrop_x_node = device_offset_node.getNextSibling();
        backdrop_x_node = XMLUtils.getNextElement(backdrop_x_node);
        if (backdrop_x_node == null || !backdrop_x_node.getNodeName().equals("robot_position_at_backdrop_x")
                || camera_center_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'robot_position_at_backdrop_x' not found");

        double backdropX = getDoubleContent(backdrop_x_node.getTextContent(), "robot_position_at_backdrop_x");

        // <robot_position_at_backdrop_y>
        Node backdrop_y_node = backdrop_x_node.getNextSibling();
        backdrop_y_node = XMLUtils.getNextElement(backdrop_y_node);
        if (backdrop_y_node == null || !backdrop_y_node.getNodeName().equals("robot_position_at_backdrop_y")
                || camera_center_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'robot_position_at_backdrop_y' not found");

        double backdropY = getDoubleContent(backdrop_y_node.getTextContent(), "robot_position_at_backdrop_y");

        // <april_tag_id>
        Node apriltag_node = backdrop_y_node.getNextSibling();
        apriltag_node = XMLUtils.getNextElement(apriltag_node);
        if (apriltag_node == null || !apriltag_node.getNodeName().equals("april_tag_id")
                || apriltag_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'april_tag_id' not found");

        int aprilTagId;
        try {
            aprilTagId = Integer.parseInt(apriltag_node.getTextContent());
        } catch (NumberFormatException nex) {
            throw new AutonomousRobotException(TAG, "Invalid number format in element 'april_tag_id'");
        }

        // <approach>
        Node approach_node = apriltag_node.getNextSibling();
        approach_node = XMLUtils.getNextElement(approach_node);
        if (approach_node == null || !approach_node.getNodeName().equals("approach")
                || approach_node.getTextContent().isEmpty())
            throw new AutonomousRobotException(TAG, "Element 'approach' not found");

        StartParameters.ApproachBackdrop approachBackdrop;
        switch (approach_node.getTextContent()) {
            case "Strafe to" -> approachBackdrop = StartParameters.ApproachBackdrop.STRAFE_TO;
            case "Turn to" -> approachBackdrop = StartParameters.ApproachBackdrop.TURN_TO;
            default ->
                throw new AutonomousRobotException(TAG, "Invalid approach to backdrop");
        }

        startParameters = new StartParameters(robotWidth, robotHeight, cameraCenter, cameraOffset,
                deviceCenter, deviceOffset, backdropX, backdropY, aprilTagId, approachBackdrop);

        RobotLogCommon.c(TAG, "In StartParametersXML; opened and parsed the XML file");
    }

    public StartParameters getStartParameters() {
        return startParameters;
    }

    private double getDoubleContent(String pElementText, String pElementName) {
        try {
            return Double.parseDouble(pElementText);
        } catch (NumberFormatException nex) {
            throw new AutonomousRobotException(TAG, "Invalid number format in element '" + pElementName + "'");
        }
    }

}