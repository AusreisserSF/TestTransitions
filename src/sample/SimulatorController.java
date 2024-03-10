package sample;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

public class SimulatorController {

    @FXML
    public BorderPane ftc_center_stage_display;

    @FXML
    public GridPane field;

    @FXML
    public Spinner april_tag_spinner_id;

    @FXML
    public ChoiceBox direction_choice_id;

    @FXML
    public TextField robot_width_id;

    @FXML
    public TextField robot_height_id;

}
