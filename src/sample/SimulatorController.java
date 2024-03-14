package sample;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
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
    public Spinner april_tag_spinner_id;
    @FXML
    public ChoiceBox approach_choice_id;

}
