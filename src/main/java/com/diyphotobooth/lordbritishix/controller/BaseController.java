package com.diyphotobooth.lordbritishix.controller;

import com.diyphotobooth.lordbritishix.scene.BaseScene;
import com.diyphotobooth.lordbritishix.utils.StageManager;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * Base class for setting up controllers
 */
public abstract class BaseController {
    private final StageManager stageManager;
    private BaseScene scene;

    public BaseController(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public void setScene(BaseScene baseScene) {
        this.scene = baseScene;
    }

    public StageManager getStageManager() {
        return stageManager;
    }

    /**
     * Returns the scene that this controller manages
     */
    public BaseScene getScene() {
        return scene;
    }

    /**
     * Interface to handle actions
     */
    public void handle(Node node, ActionEvent e) {

    }

    public void handle(Node node, KeyEvent e) {

    }

    public void handle(Node node, MouseEvent e) {

    }

}

