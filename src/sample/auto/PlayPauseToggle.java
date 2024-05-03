package sample.auto;

import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

//**TODO Move this class inside of CenterStageBackdrop so that it has access
// to class fields. Make private.

public class PlayPauseToggle {

    private enum PlayPauseButtonStateOnPress {FIRST_PLAY, RESUME_PLAY, PAUSE}
    private PlayPauseButtonStateOnPress playPauseButtonStateOnPress;

    private final Button playPauseButton;
    private final SequentialTransition sequentialTransition;

    // Assume when this class is constructed that the Play button has already been
    // pressed.
    public PlayPauseToggle(Button pPlayPauseButton, SequentialTransition pSequentialTransaction) {
        playPauseButton = pPlayPauseButton;
        sequentialTransition = pSequentialTransaction;
        playPauseButtonStateOnPress = PlayPauseButtonStateOnPress.PAUSE; // state for the next button press

        // When the SequentialTransitions are complete, disable the play/pause button.
        sequentialTransition.statusProperty().addListener((observableValue, oldValue, newValue) -> {
                    if (newValue == Animation.Status.STOPPED)
                        playPauseButton.setDisable(true);
                }
        );

        // Action event for the play/pause button.
        EventHandler<ActionEvent> event = e -> {
            switch (playPauseButtonStateOnPress) {
                case FIRST_PLAY -> {
                    //**TODO Clear positioning robot, FOV lines

                    /*
                                centerStageRobot = new RobotFXCenterStageLG(robotWidthIn, robotHeightIn, Color.GREEN,
                    cameraCenterFromRobotCenter, cameraOffsetFromRobotCenter, deviceCenterFromRobotCenter, deviceOffsetFromRobotCenter,
                    startingPosition, startingRotation);
                               Group robot = centerStageRobot.getRobot();
            field.getChildren().add(robot);

                     */

                    playPauseButton.setText("Pause");
                    playPauseButtonStateOnPress = PlayPauseButtonStateOnPress.PAUSE;
                    sequentialTransition.play();
                }
                case RESUME_PLAY -> {
                    if (sequentialTransition.getStatus() != Animation.Status.STOPPED) {
                        playPauseButton.setText("Pause");
                        playPauseButtonStateOnPress = PlayPauseButtonStateOnPress.PAUSE;
                        sequentialTransition.play();
                    }
                }
                case PAUSE -> {
                    if (sequentialTransition.getStatus() != Animation.Status.STOPPED) {
                        sequentialTransition.pause();
                        playPauseButton.setText("Play");
                        playPauseButtonStateOnPress = PlayPauseButtonStateOnPress.RESUME_PLAY;
                    }
                }
                default -> throw new RuntimeException("Invalid button state " + playPauseButtonStateOnPress);
            }
        };

        playPauseButton.setOnAction(event);
        playPauseButton.setText("Play");
        playPauseButtonStateOnPress = PlayPauseButtonStateOnPress.FIRST_PLAY;
    }
}