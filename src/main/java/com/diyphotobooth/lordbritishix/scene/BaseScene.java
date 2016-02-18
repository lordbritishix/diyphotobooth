package com.diyphotobooth.lordbritishix.scene;

import com.diyphotobooth.lordbritishix.controller.BaseController;
import javafx.beans.NamedArg;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

/**
 * Base class for Scenes
 */
public abstract class BaseScene extends Scene {
    private BaseController controller;

    public BaseScene(@NamedArg("root") Parent root,
                     BaseController controller,
                     double width,
                     double height) {
        super(root, width, height);
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
