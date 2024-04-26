package sample.auto;

import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class PlayPauseToggle {

    private enum ButtonStateOnPress {PLAY, PAUSE}

    private ButtonStateOnPress buttonStateOnPress;

    private final Button playPauseButton;
    private final SequentialTransition sequentialTransition;

    // Assume when this class is constructed that the Play button has already been
    // pressed.
    public PlayPauseToggle(Button pPlayPauseButton, SequentialTransition pSequentialTransaction) {
        playPauseButton = pPlayPauseButton;
        sequentialTransition = pSequentialTransaction;
        buttonStateOnPress = ButtonStateOnPress.PAUSE; // state for the next button press

        // When the SequentialTransitions are complete, disable the play/pause button.
        sequentialTransition.statusProperty().addListener((observableValue, oldValue, newValue) -> {
                    if (newValue == Animation.Status.STOPPED)
                        playPauseButton.setDisable(true);
                }
        );

        // Action event for the play/pause button.
        EventHandler<ActionEvent> event = e -> {
            switch (buttonStateOnPress) {
                case PLAY -> {
                    if (sequentialTransition.getStatus() != Animation.Status.STOPPED) {
                        playPauseButton.setText("Pause");
                        buttonStateOnPress = ButtonStateOnPress.PAUSE;
                        sequentialTransition.play();
                    }
                }
                case PAUSE -> {
                    if (sequentialTransition.getStatus() != Animation.Status.STOPPED) {
                        sequentialTransition.pause();
                        playPauseButton.setText("Play");
                        buttonStateOnPress = ButtonStateOnPress.PLAY;
                    }
                }
                default -> throw new RuntimeException("Invalid button state " + buttonStateOnPress);
            }
        };

        playPauseButton.setOnAction(event);

        playPauseButton.setText("Pause");
        buttonStateOnPress = ButtonStateOnPress.PAUSE;
        sequentialTransition.play();
    }
}