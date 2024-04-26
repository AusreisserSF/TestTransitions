package sample.auto.fx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class CenterStageControllerLG {

    @FXML
    public BorderPane ftc_center_stage_display;
    @FXML
    public Pane field;
    @FXML
    public GridPane start_parameters;
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
    public TextField robot_position_at_backdrop_x;
    @FXML
    public TextField robot_position_at_backdrop_y;
    @FXML
    public Spinner<Integer> april_tag_spinner;
    @FXML
    public ToggleGroup approach_toggle;

}
