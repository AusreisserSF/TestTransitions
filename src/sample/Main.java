package sample;

import javafx.application.Application;

public class Main {

    public static void main(String[] args) {

        // The name of the application is on the commend line.
        switch (args[0]) {
            case "SimpleTest" -> Application.launch(SimpleTest.class, args);
            case "BoundsDemo" -> Application.launch(BoundsDemo.class, args);
            case "Transitions" -> Application.launch(Transitions.class, args);
            case "Text" -> Application.launch(TextInput.class, args);
            case "DragAndDrop" -> Application.launch(DragAndDrop.class, args);
            case "Drag" -> Application.launch(Drag.class, args);
            case "DragPreview" -> Application.launch(DragPreviewRobot.class, args);
            case "DragCircles" -> Application.launch(DragGroupCircles.class, args);
            case "Radio" -> Application.launch(AllianceDialog.class, args);
            default -> System.out.println("Unrecognized application class name");
        }
    }
}
