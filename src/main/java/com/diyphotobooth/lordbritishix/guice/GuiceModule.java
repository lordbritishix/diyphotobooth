package com.diyphotobooth.lordbritishix.guice;

import com.diyphotobooth.lordbritishix.utils.StageManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javafx.stage.Stage;

/**
 * Sets up DI configuration
 */
public class GuiceModule extends AbstractModule {
    private final Stage primaryStage;

    public GuiceModule(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Override
    protected void configure() {
        bind(StageManager.class);
    }

    @Provides
    public Stage stageProvider() {
        return primaryStage;
    }
}
