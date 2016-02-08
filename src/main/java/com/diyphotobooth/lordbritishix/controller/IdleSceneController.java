package com.diyphotobooth.lordbritishix.controller;

import com.diyphotobooth.lordbritishix.scene.CameraScene;
import com.diyphotobooth.lordbritishix.utils.StageManager;
import com.google.inject.Inject;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;

/**
 * Controls the Idle Scene Controller
 */
public class IdleSceneController extends BaseController {
    private static final AudioClip START = new AudioClip(CameraSceneController.class.getResource("/sound/start.wav").toString());

    @Inject
    public IdleSceneController(StageManager stageManager) {
        super(stageManager);
    }

    @Override
    public void handle(Node node, MouseEvent e) {
        START.play();
        getStageManager().showScene(CameraScene.class);
    }

    @Override
    public void shutdown() {

    }
}
