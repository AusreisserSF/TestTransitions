package sample;

import javafx.application.Application;

public class Main {

    public static void main(String[] args) {

        // The name of the application is on the commend line.
        switch (args[0]) {
            case "SimpleTest" -> Application.launch(SimpleTest.class, args);
            case "BoundsDemo" -> Application.launch(BoundsDemo.class, args);
            case "Transitions" -> Application.launch(Transitions.class, args);
            case "Backdrop" -> Application.launch(Backdrop.class, args);
            case "Text" -> Application.launch(TextInput.class, args);
            default -> System.out.println("Unrecognized application class name");
        }
    }
}
