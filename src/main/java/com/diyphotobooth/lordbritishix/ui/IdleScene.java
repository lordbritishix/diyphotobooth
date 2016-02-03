package com.diyphotobooth.lordbritishix.ui;

import com.diyphotobooth.lordbritishix.controller.IdleSceneController;
import com.google.inject.Inject;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class IdleScene extends BaseScene {
    @Inject
    public IdleScene(IdleSceneController baseController) {
        super(new StackPane(), baseController);
        Text text = new Text("Press any key to start");
        text.setId("welcome-text");

        text.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 40));
        text.setFill(Color.WHITE);

        setOnKeyReleased(p -> getController().handle(null, p));

        getRootPane().getChildren().add(text);
    }
}
