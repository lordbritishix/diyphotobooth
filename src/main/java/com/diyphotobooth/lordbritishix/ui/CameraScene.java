package com.diyphotobooth.lordbritishix.ui;

import com.diyphotobooth.lordbritishix.controller.CameraSceneController;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

/**
 * Camera Scene is responsible for:
 * 1. Displaying the view finder
 * 2.
 */
public class CameraScene extends BaseScene {
    @Inject
    public CameraScene(CameraSceneController controller) {
        super(new StackPane(), controller);

//        Text text = new Text("Camera Scene");
//        text.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 40));
//        text.setFill(Color.GREEN);
//        getRootPane().getChildren().add(text);

        Button b = new Button("Show Idle Scene");
        b.setOnAction(p -> controller.handle(b, p));
        getRootPane().getChildren().add(b);
    }
}
