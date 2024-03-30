package sample;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class SimulatorController {

    @FXML
    public BorderPane ftc_center_stage_display;
    @FXML
    public Pane field;
    public Label alliance_id;
    @FXML
    public TextField robot_width_id;
    @FXML
    public TextField robot_height_id;
    @FXML
    public TextField camera_center_from_robot_center_id;
    @FXML
    public TextField camera_offset_from_robot_center_id;
    @FXML
    public TextField device_center_from_robot_center_id;
    @FXML
    public TextField device_offset_from_robot_center_id;
    @FXML
    public TextField position_x_id;
    @FXML
    public TextField position_y_id;
    @FXML
    public Spinner<Integer> april_tag_spinner_id;
    @FXML
    public ToggleGroup approach_toggle_id;

}
