package com.diyphotobooth.lordbritishix.scene;

import com.diyphotobooth.lordbritishix.controller.CameraSceneController;
import com.diyphotobooth.lordbritishix.utils.StageManager;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Camera Scene is responsible for:
 * 1. Displaying the view finder
 * 2. Walking the user through the capture image process
 */
@Slf4j
public class CameraScene extends BaseScene {
    private final ImageView imageView;
    private final StageManager stageManager;
    private final Counter counter;
    private final Countdown countdown;

    @Inject
    public CameraScene(CameraSceneController controller,
                       StageManager stageManager,
                       @Named("screen.width") double width,
                       @Named("screen.height") double height) {
        super(new StackPane(), controller, width, height);
        this.stageManager = stageManager;

        this.imageView = new ImageView();
        this.imageView.setPreserveRatio(true);

        this.countdown = new Countdown();

        counter = new Counter(0, 0);

        getRootPane().getChildren().add(0, imageView);
        getRootPane().getChildren().add(1, counter);
        getRootPane().getChildren().add(1, countdown);

        getRootPane().setOnMouseClicked(p -> getController().handle(null, p));

        StackPane.setAlignment(counter, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(counter, new Insets(20));
    }

    public void setCountdownValueAndStart(int countdownFrom, int startDelay, Consumer<Void> countdownCompleteCallback) {
        countdown.setCountdownFrom(countdownFrom, startDelay, countdownCompleteCallback);
        countdown.start();
    }

    public void setCounterValue(int currentValue, int finalValue) {
        counter.setCounterValue(currentValue, finalValue);
    }

    public void setCountdownText(String text) {
        countdown.showText(text);
    }

    public void clearCounterValue() {
        counter.clear();
    }

    public void setCameraImage(InputStream is) {
        imageView.fitWidthProperty().bind(stageManager.getCurrentStage().widthProperty());
        imageView.fitHeightProperty().bind(stageManager.getCurrentStage().heightProperty());
        imageView.setImage(new Image(is));
    }

    public void showRetry() {
        try(InputStream is = CameraSceneController.class.getClass().getResourceAsStream("/img/retry.png")) {
            imageView.fitWidthProperty().unbind();
            imageView.fitHeightProperty().unbind();
            imageView.setFitWidth(100.0d);
            imageView.setFitHeight(100.0d);
            imageView.setImage(new Image(is));
        } catch (IOException e) {
            log.error("Unable to load retry.png", e);
        }
    }

    public void showCheckmark() {
        try(InputStream is = CameraSceneController.class.getClass().getResourceAsStream("/img/checkmark.png")) {
            imageView.fitWidthProperty().unbind();
            imageView.fitHeightProperty().unbind();
            imageView.setFitWidth(100.0d);
            imageView.setFitHeight(100.0d);
            imageView.setImage(new Image(is));
        } catch (IOException e) {
            log.error("Unable to load checkmark.png", e);
        }
    }

    public void showWrong() {
        try(InputStream is = CameraSceneController.class.getClass().getResourceAsStream("/img/wrong.png")) {
            imageView.fitWidthProperty().unbind();
            imageView.fitHeightProperty().unbind();
            imageView.setFitWidth(100.0d);
            imageView.setFitHeight(100.0d);
            imageView.setImage(new Image(is));
        } catch (IOException e) {
            log.error("Unable to load wrong.png", e);
        }
    }


    public void showLoading() {
        try(InputStream is = CameraSceneController.class.getClass().getResourceAsStream("/img/hourglass.gif")) {
            imageView.fitWidthProperty().unbind();
            imageView.fitHeightProperty().unbind();
            imageView.setFitWidth(100.0d);
            imageView.setFitHeight(100.0d);
            imageView.setImage(new Image(is));
        } catch (IOException e) {
            log.error("Unable to load hourglass.gif", e);
        }
    }
}
