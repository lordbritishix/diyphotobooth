package com.diyphotobooth.lordbritishix.guice;

import com.diyphotobooth.lordbritishix.utils.StageManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Sets up DI configuration
 */
public class GuiceModule extends AbstractModule {
    private final Stage primaryStage;
    private final Path settingsFolder;

    public GuiceModule(Stage primaryStage, Path settingsFolder) {
        this.primaryStage = primaryStage;
        this.settingsFolder = settingsFolder;
    }

    @Override
    protected void configure() {
        bind(StageManager.class);

        Properties properties = null;
        try {
            properties = loadFromFile(settingsFolder);
            bindConstant().annotatedWith(Names.named("ipcamera.hostName"))
                    .to(properties.get("ipcamera.hostName").toString());
            bindConstant().annotatedWith(Names.named("ipcamera.portNumber"))
                    .to(Integer.parseInt(properties.get("ipcamera.portNumber").toString()));
        } catch (IOException e) {
            throw new RuntimeException("Unable to load settings file specified by " + settingsFolder.toString(), e);
        }
    }

    private Properties loadFromFile(Path path) throws IOException {
        Properties properties = new Properties();
        try (InputStream is = new FileInputStream(path.toFile())) {
            properties.load(is);
            return  properties;
        }
    }

    @Provides
    public Stage stageProvider() {
        return primaryStage;
    }
}
