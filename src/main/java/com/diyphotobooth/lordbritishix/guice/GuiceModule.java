package com.diyphotobooth.lordbritishix.guice;

import com.diyphotobooth.lordbritishix.utils.StageManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.gm4java.engine.GMService;
import org.gm4java.engine.support.SimpleGMService;

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
            bindConstant().annotatedWith(Names.named("buffer.size"))
                    .to(Integer.parseInt(properties.get("buffer.size").toString()));
            bindConstant().annotatedWith(Names.named("countdown.length.sec"))
                    .to(Integer.parseInt(properties.get("countdown.length.sec").toString()));
            bindConstant().annotatedWith(Names.named("resources.folder"))
                    .to(properties.get("resources.folder").toString());
            bindConstant().annotatedWith(Names.named("snapshot.folder"))
                    .to(properties.get("snapshot.folder").toString());
            bindConstant().annotatedWith(Names.named("photo.count"))
                    .to(properties.get("photo.count").toString());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load settings file specified by " + settingsFolder.toString(), e);
        }

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        bindConstant().annotatedWith(Names.named("screen.width"))
                .to(primaryScreenBounds.getWidth());
        bindConstant().annotatedWith(Names.named("screen.height"))
                .to(primaryScreenBounds.getHeight());
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
    GMService gmServiceProvider() {
        return new SimpleGMService();
    }
}
