package com.diyphotobooth.lordbritishix.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
/**
 * Utilities for managing Sessions
 */
public class SessionUtils {
    /**
     * Creates a new session.
     */
    public Session newSession(int photoCount, Path snapshotFolder) throws IOException {
        Session session = new Session(photoCount);
        writeSessionMetadataToDisk(snapshotFolder, session);

        return session;
    }

    /**
     * Advances the session to take the next photo and associates the current photo to the name of the captured image
     */
    public int next(Session session, String imageName) {
        int next = session.nextPhoto();
        session.getImageMap().put(next, imageName);
        return next;
    }

    public String writeImageToCurrentSession(Session session, InputStream is, Path snapshotFolder) throws IOException {
        String imageName = session.getNumberOfPhotosAlreadyTaken() + ".jpg";
        Path writeDir = Files.createDirectories(Paths.get(snapshotFolder.toString(), session.getSessionId().toString()));
        Path photo = Files.createFile(Paths.get(writeDir.toString(), imageName));

        try(OutputStream os = new FileOutputStream(photo.toFile())) {
            IOUtils.copy(is, os);
            writeSessionMetadataToDisk(snapshotFolder, session);
        }

        return imageName;
    }

    public Path getSessionDir(Path snapshotDir, Session session) {
        return Paths.get(snapshotDir.toString(), session.getSessionId().toString());
    }

    public void updateSessionStateAndPersistQuietly(Path snapshotDir, Session session, Session.State newState) {
        session.setState(newState);
        try {
            writeSessionMetadataToDisk(snapshotDir, session);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeSessionMetadataToDisk(Path snapshotDir, Session session) throws IOException {
        Files.createDirectories(getSessionDir(snapshotDir, session));
        Path metadata = Paths.get(getSessionDir(snapshotDir, session).toString(), "metadata.json");
        Path tempFile = Files.createTempFile("metadata_", ".json");

        try (OutputStream os = new FileOutputStream(tempFile.toFile())) {
            IOUtils.copy(new ByteArrayInputStream(session.toJson().getBytes(StandardCharsets.UTF_8)), os);
        }
        Files.move(tempFile, metadata, StandardCopyOption.REPLACE_EXISTING);
    }
}
