package com.diyphotobooth.lordbritishix.controller;

import com.diyphotobooth.lordbritishix.scene.CameraScene;
import com.diyphotobooth.lordbritishix.utils.StageManager;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Controls the Idle Scene Controller
 */
public class IdleSceneController extends BaseController {
    @Inject
    public IdleSceneController(StageManager stageManager) {
        super(stageManager);
    }

    @Override
    public void handle(Node node, MouseEvent e) {
        Platform.runLater(() -> getStageManager().showScene(CameraScene.class));
    }

    @Override
    public void shutdown() {

    }
}
