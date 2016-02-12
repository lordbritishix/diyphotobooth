package com.diyphotobooth.lordbritishix.guice;

import com.diyphotobooth.lordbritishix.model.Template;
import com.diyphotobooth.lordbritishix.utils.StageManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            bindConstant().annotatedWith(Names.named("buffer.size"))
                    .to(Integer.parseInt(properties.get("buffer.size").toString()));
            bindConstant().annotatedWith(Names.named("countdown.length.sec"))
                    .to(Integer.parseInt(properties.get("countdown.length.sec").toString()));
            bindConstant().annotatedWith(Names.named("template.filename"))
                    .to(properties.get("template.filename").toString());
            bindConstant().annotatedWith(Names.named("snapshot.folder"))
                    .to(properties.get("snapshot.folder").toString());
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

    @Provides
    Template templateProvider() throws IOException {
        Properties properties = loadFromFile(settingsFolder);
        File templateFile = Paths.get(properties.getProperty("template.filename")).toFile();
        try (InputStream is = new FileInputStream(templateFile)) {
            return Template.fromJson(is);
        }
    }
}
