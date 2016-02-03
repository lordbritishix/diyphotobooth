package com.diyphotobooth.lordbritishix.utils;

import com.diyphotobooth.lordbritishix.scene.BaseScene;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import javafx.stage.Stage;

@Singleton
/**
 * Responsible for managing scenes and switching between them
 */
public class StageManager {
    private final Stage currentStage;
    private final Injector injector;

    @Inject
    public StageManager(Stage currentStage, Injector injector) {
        this.currentStage = currentStage;
        this.injector = injector;
    }

    public void showScene(Class<? extends BaseScene> scene) {
        currentStage.setScene(injector.getInstance(scene));

        if (!currentStage.isShowing()) {
            currentStage.show();
        }
    }
}
