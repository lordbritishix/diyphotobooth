package com.diyphotobooth.lordbritishix.scene;

import com.diyphotobooth.lordbritishix.controller.IdleSceneController;
import com.google.inject.Inject;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/**
 * Idle Scene is responsible for:
 * 1. Displaying the welcome message
 * 2. Transitioning to the camera screen when a key is pressed
 */
public class IdleScene extends BaseScene {
    @Inject
    public IdleScene(IdleSceneController baseController) {
        super(new StackPane(), baseController);
        Text text = new Text("Tap anywhere to start");
        text.setId("welcome-text");

        setOnMouseClicked(p -> getController().handle(null, p));

        getRootPane().getChildren().add(text);
    }
}
