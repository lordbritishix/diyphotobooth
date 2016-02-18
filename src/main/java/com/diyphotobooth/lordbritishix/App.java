package com.diyphotobooth.lordbritishix;

import com.diyphotobooth.lordbritishix.guice.GuiceModule;
import com.diyphotobooth.lordbritishix.jobprocessor.DoneProcessor;
import com.diyphotobooth.lordbritishix.jobprocessor.JobProcessor;
import com.diyphotobooth.lordbritishix.jobprocessor.MontageProcessor;
import com.diyphotobooth.lordbritishix.jobprocessor.PrintProcessor;
import com.diyphotobooth.lordbritishix.scene.BaseScene;
import com.diyphotobooth.lordbritishix.scene.IdleScene;
import com.diyphotobooth.lordbritishix.utils.StageManager;
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;

@Slf4j
public class App extends Application {
    private StageManager stageManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Injector injector = Guice.createInjector(
                new GuiceModule(primaryStage,
                        Paths.get("/home/lordbritishix/src/diyphotobooth/settings/dashboard.properties")));

        stageManager = injector.getInstance(StageManager.class);
        primaryStage.setTitle("Jim's DIY Photo Booth");
        primaryStage.getIcons().add(new Image(App.class.getResourceAsStream("/icon.png")));

        stageManager.showScene(IdleScene.class);

        JobProcessor processor = injector.getInstance(JobProcessor.class);
        MontageProcessor montageProcessor = injector.getInstance(MontageProcessor.class);
        PrintProcessor printProcessor = injector.getInstance(PrintProcessor.class);
        DoneProcessor doneProcessor = injector.getInstance(DoneProcessor.class);
        processor.start(ImmutableList.of(montageProcessor, printProcessor, doneProcessor));


        primaryStage.setOnCloseRequest(p -> {
            log.info("Shutting down");
            BaseScene scene = stageManager.getCurrentScene();
            try {
                scene.getController().shutdown();
                processor.stop();
            } catch (Exception e) {
                log.error("Unable to shutdown controller", e);
            }
        });
    }
}
