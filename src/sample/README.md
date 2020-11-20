The test program reproduces the problem. I understand why the exception is thrown but I would like to know how can I work around it or use a different construct in JavaFX to get what I want.

The full application is a robot simulator with multiple robots that move autonomously, independently, and simultaneously around a field. Each robot has its own SequentialTransition for its particular set of movements. The program adds the SequentialTransitions to a ParallelTransition, which it then plays. Everything was fine until I put in a listener that notices if a robot runs into an obstacle. I've simplified the collision detection in the test program to apply to only one robot and one wall. The point of the error is marked with //** BROKEN!! IllegalStateException on next line.

I really do want to stop the SequentialTransition for a robot that runs into an obstacle but let the other robot(s) continue. How can I do this?

The error comes up in Java 8 but also in Java 11 and JavaFX 15.

Fixed according to the answer from swpalmer on
https://stackoverflow.com/questions/64921759/javafx-sequentialtransition-illegalstateexception-cannot-stop-when-embedded-in
