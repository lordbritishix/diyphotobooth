package com.diyphotobooth.lordbritishix.scene;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class Countdown extends Group {
    private final Timeline timeline;
    private final Text countdownText;
    private static final AudioClip BEEP = new AudioClip(Countdown.class.getResource("/sound/beep.wav").toString());
    private static final AudioClip SNAP = new AudioClip(Countdown.class.getResource("/sound/snap.wav").toString());

    public Countdown() {
        countdownText = new Text();
        countdownText.setId("countdown");
        timeline = new Timeline();
        getChildren().add(countdownText);
    }

    public void setCountdownFrom(int countdownFrom, int startDelay, Consumer<Void> countdownCompleteHandler) {
        timeline.getKeyFrames().clear();
        int finalVal = startDelay + countdownFrom;
        for (int x = startDelay; x < finalVal; ++x) {
            int value = countdownFrom;
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(x + 1), p -> {
                int val = value - 1;
                countdownText.setText(val != 0 ? String.valueOf(value - 1) : "");

                if (val != 0) {
                    BEEP.play();
                    countdownText.setText(String.valueOf(value - 1));
                }
                else {
                    SNAP.play();
                    countdownText.setText("");
                }
            }));

            countdownFrom--;
        }
        timeline.setOnFinished(p -> countdownCompleteHandler.accept(null));
    }

    public void showText(String text) {
        countdownText.setText(text);
    }

    public void start() {
        timeline.playFromStart();
    }
}
