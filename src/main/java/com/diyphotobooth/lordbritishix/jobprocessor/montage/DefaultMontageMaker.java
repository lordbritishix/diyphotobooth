package com.diyphotobooth.lordbritishix.jobprocessor.montage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMService;
import org.gm4java.engine.GMServiceException;
import com.diyphotobooth.lordbritishix.model.Session;
import com.diyphotobooth.lordbritishix.model.SessionUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMontageMaker implements MontageMaker {
    private final GMService service;
    private final Path resourcesFolder;
    private final SessionUtils sessionUtils;
    private final Path snapshotDir;

    @Inject
    public DefaultMontageMaker(GMService service,
                               @Named("resources.folder") String resourcesFolder,
                               @Named("snapshot.folder") String snapshotDir,
                               SessionUtils sessionUtils) {
        this.service = service;
        this.resourcesFolder = Paths.get(resourcesFolder);
        this.sessionUtils = sessionUtils;
        this.snapshotDir = Paths.get(snapshotDir);
    }

    @Override
    public Path apply(Session session, Path sessionDir) {
        Path artPath = Paths.get(resourcesFolder.toString(), "defaultArt.png");
        Path patternPath = Paths.get(resourcesFolder.toString(), "pattern.jpg");

        Path imagePath = Paths.get(sessionDir.toString(), "tmp.grid.png");
        Path montagePath = Paths.get(sessionDir.toString(), "montage.jpg");

        GMConnection connection = null;
        try {
            connection = service.getConnection();

            List<String> imagesWithPath = session.getImagesAsList().stream()
                                            .map(p -> Paths.get(sessionDir.toString(), p).toString())
                                            .collect(Collectors.toList());

            //Assumes image has been flipped
            for (String image : imagesWithPath) {
                List<String> cropCommand = Lists.newArrayList();
                cropCommand.add("convert");
                cropCommand.add(image);
                cropCommand.add("-rotate");
                cropCommand.add("180");
                cropCommand.add(image);
                connection.execute(cropCommand);
            }

            //Center-crop images to 640x590
            for (String image : imagesWithPath) {
                List<String> cropCommand = Lists.newArrayList();
                cropCommand.add("convert");
                cropCommand.add(image);
                cropCommand.add("-thumbnail");
                cropCommand.add("740x590^");
                cropCommand.add("-gravity");
                cropCommand.add("center");
                cropCommand.add("-extent");
                cropCommand.add("740x590");
                cropCommand.add(image + "_cropped.tmp");
                connection.execute(cropCommand);
            }

            //Compose image grid
            List<String> gridCommand = Lists.newArrayList();
            gridCommand.add("montage");
            gridCommand.add("-geometry");
            gridCommand.add("740x590+5+5");
            gridCommand.add("-texture");
            gridCommand.add(patternPath.toString());
            gridCommand.add("-label");
            gridCommand.add("");
            gridCommand.add("-tile");
            gridCommand.add("2x2");
            gridCommand.addAll(imagesWithPath.stream().map(p -> p + "_cropped.tmp").collect(Collectors.toList()));
            gridCommand.add(imagePath.toString());
            connection.execute(gridCommand);

            //Combine art and image grid
            List<String> montageCommand = Lists.newArrayList();
            montageCommand.add("montage");
            montageCommand.add("-geometry");
            montageCommand.add("+0+0^");
            montageCommand.add("-label");
            montageCommand.add("");
            montageCommand.add("-tile");
            montageCommand.add("2x1");
            montageCommand.add(artPath.toString());
            montageCommand.add(imagePath.toString());
            montageCommand.add(montagePath.toString());
            connection.execute(montageCommand);

            Files.list(sessionDir).forEach(p -> {
                if (p.getFileName().toString().endsWith(".tmp") || (p.getFileName().toString().startsWith("tmp."))) {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        log.error("Unable to delete {}", p.toString());
                    }
                }
            });

            return montagePath;
        }
        catch(Exception e) {
            log.error("Unable to compose the montage for session: {}", session, e);
            return null;
        }
        finally {
            closeConnectionQuietly(connection);
        }
    }

    private void closeConnectionQuietly(GMConnection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (GMServiceException e) {
        }
    }
}
