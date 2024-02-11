package sample;

import javafx.application.Application;

import java.util.ArrayList;
import java.util.EnumSet;

public class Main {

    enum DriveSpeed {FULL, HALF}

    public static void main(String[] args) {

        EnumSet<DriveSpeed> speedSet = EnumSet.allOf(DriveSpeed.class);
        ArrayList<DriveSpeed> toggleValues = new ArrayList<>();
        speedSet.forEach((e -> toggleValues.add(e)));
        int toggleIndex = 0;

        System.out.println("First toggle index " + toggleIndex + ", value " + toggleValues.get(toggleIndex));
        for (int i = 0; i < 5; i++) {
            toggleIndex = (toggleIndex < (toggleValues.size() - 1)) ? ++toggleIndex : 0;
            System.out.println("Toggle index " + toggleIndex + ", value " + toggleValues.get(toggleIndex));
        }

        // The name of the application is on the commend line.
        switch (args[0]) {
            case "SimpleTest" -> Application.launch(SimpleTest.class, args);
            case "BoundsDemo" -> Application.launch(BoundsDemo.class, args);
            case "Transitions" -> Application.launch(Transitions.class, args);
            case "Curves" -> Application.launch(Curves.class, args);
            default -> System.out.println("Unrecognized application class name");
        }
    }
}
