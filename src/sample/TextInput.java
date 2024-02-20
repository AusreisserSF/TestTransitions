package sample;


// From https://www.pragmaticcoding.ca/javafx/textformatter1

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.util.function.UnaryOperator;

public class TextInput extends Application {

    private ObjectProperty<Integer> valueProperty = new SimpleObjectProperty<>(0);

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(new TestPane(), 300, 100);
        valueProperty.addListener(((observable, oldValue, newValue) -> {
            System.out.println("Value changed -> Old Value: " + oldValue + ", New Value: " + newValue);
        }));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public class TestPane extends BorderPane {
        public TestPane() {
            TextField textField = new TextField();
            // new TextFormatter(new PositiveIntegerStringConverter(), 0, new PositiveIntegerFilter())
            TextFormatter<Integer> textFormatter = new TextFormatter(new IntegerStringConverter()
                    , 0, new IntegerFilter());
           textFormatter.valueProperty().bindBidirectional(valueProperty);
            textField.setTextFormatter(textFormatter);
            setCenter(new HBox(6, new Text("TextField 1"), textField));
        }
    }

    public class IntegerFilter implements UnaryOperator<TextFormatter.Change> {

        @Override
        public TextFormatter.Change apply(TextFormatter.Change change) {
            if (change.getControlNewText().matches("-?([1-9][0-9]*)?")) {
                return change;
            }
            return null;
        }
    }

}

