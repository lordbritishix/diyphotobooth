package com.diyphotobooth.lordbritishix.ui;

import com.diyphotobooth.lordbritishix.controller.BaseController;
import com.diyphotobooth.lordbritishix.utils.Constants;

import javafx.beans.NamedArg;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

/**
 * Base class for Scenes
 */
public abstract class BaseScene extends Scene {
    private BaseController controller;

    public BaseScene(@NamedArg("root") Parent root, BaseController controller) {
        super(root, Constants.WIDTH, Constants.HEIGHT);
        this.controller = controller;
        this.controller.setScene(this);

        getStylesheets().add(BaseScene.class.getResource("/css/Style.css").toExternalForm());

    }

    public Pane getRootPane() {
        return (Pane) this.getRoot();
    }

    public BaseController getController() {
        return controller;
    }
}
