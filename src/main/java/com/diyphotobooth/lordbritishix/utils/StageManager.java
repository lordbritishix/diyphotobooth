package com.diyphotobooth.lordbritishix.utils;

import com.diyphotobooth.lordbritishix.scene.BaseScene;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

@Singleton
/**
 * Responsible for managing scenes and switching between them
 */
public class StageManager {
    private final Stage currentStage;
    private final Injector injector;
    private BaseScene currentScene;

    @Inject
    public StageManager(Stage currentStage, Injector injector) {
        this.currentStage = currentStage;
        this.injector = injector;
    }

    public Stage getCurrentStage() {
        return currentStage;
    }

    public void showScene(Class<? extends BaseScene> scene) {
        currentScene = injector.getInstance(scene);
        currentStage.setScene(currentScene);
        currentStage.show();

        currentScene.getController().sceneLoaded();
    }

    public BaseScene getCurrentScene() {
        return  currentScene;
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
