package com.diyphotobooth.lordbritishix.controller;

import com.diyphotobooth.lordbritishix.ui.IdleScene;
import com.diyphotobooth.lordbritishix.utils.StageManager;
import com.google.inject.Inject;

import javafx.scene.Node;
import javafx.scene.input.KeyEvent;

/**
 * Controls the Camera Scene
 */
public class CameraSceneController extends BaseController {
    @Inject
    public CameraSceneController(StageManager stageManager) {
        super(stageManager);
    }

    @Override
    public void handle(Node node, KeyEvent e) {
        getStageManager().showScene(IdleScene.class);
    }
}
