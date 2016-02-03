package com.diyphotobooth.lordbritishix;

import com.diyphotobooth.lordbritishix.guice.GuiceModule;
import com.diyphotobooth.lordbritishix.ui.IdleScene;
import com.diyphotobooth.lordbritishix.utils.StageManager;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    private StageManager stageManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Injector injector = Guice.createInjector(new GuiceModule(primaryStage));

        stageManager = injector.getInstance(StageManager.class);
        primaryStage.setTitle("Jim's DIY Photo Booth");
//        primaryStage.setAlwaysOnTop(true);
//        primaryStage.setFullScreen(true);

        stageManager.showScene(IdleScene.class);
    }
}
