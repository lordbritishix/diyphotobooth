package com.diyphotobooth.lordbritishix.scene;

import com.diyphotobooth.lordbritishix.controller.CameraSceneController;
import com.diyphotobooth.lordbritishix.utils.StageManager;
import com.google.inject.Inject;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Camera Scene is responsible for:
 * 1. Displaying the view finder
 * 2. Walking the user through the capture image process
 */
@Slf4j
public class CameraScene extends BaseScene {
    private final ImageView imageView;
    private final StageManager stageManager;

    @Inject
    public CameraScene(CameraSceneController controller, StageManager stageManager) {
        super(new StackPane(), controller);
        this.stageManager = stageManager;

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        getRootPane().getChildren().add(imageView);

        this.setOnMouseClicked(p -> controller.handle(null, p));
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
