package sample;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class SimulatorController {

    @FXML
    public BorderPane ftc_center_stage_display;
    @FXML
    public Pane field;
    public Label alliance;
    @FXML
    public TextField robot_width;
    @FXML
    public TextField robot_height;
    @FXML
    public TextField camera_center_from_robot_center;
    @FXML
    public TextField camera_offset_from_robot_center;
    @FXML
    public TextField device_center_from_robot_center;
    @FXML
    public TextField device_offset_from_robot_center;
    @FXML
    public TextField position_x;
    @FXML
    public TextField position_y;
    @FXML
    public Spinner<Integer> april_tag_spinner;
    @FXML
    public ToggleGroup approach_toggle;

}
