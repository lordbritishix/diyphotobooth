package com.diyphotobooth.lordbritishix.model;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;

@Slf4j
@Singleton
public class SessionManager {
    private Stack<Session> sessions = new Stack<>();
    private final LocalDateTime sessionStart;
    private final Path snapshotFolder;
    private final UUID sessionId;

    @Inject
    public SessionManager(@Named("snapshot.folder") String snapshotFolder) {
        this.snapshotFolder = Paths.get(snapshotFolder);
        this.sessionId = UUID.randomUUID();
        sessionStart = LocalDateTime.now(ZoneOffset.UTC);
    }
    /**
     * Gets an existing session if it is not complete - otherwise, create a new one. If forceCreate is true,
     * a new session is created regardless of whether the existing session is complete.
     */
    public synchronized Session getOrCreateNewSession(int photoCount, boolean forceCreate, Template template) throws IOException {
        Session current;
        if (forceCreate) {
            current = sessions.push(new Session(photoCount, template));
        }
        else {
            if (!sessions.isEmpty()) {
                current = sessions.peek();
                if (current.isSessionFinished()) {
                    current = sessions.push(new Session(photoCount, template));
                }
            }
            else {
                current = sessions.push(new Session(photoCount, template));
            }
        }

        writeSessionMetadata(snapshotFolder, current);

        return current;
    }

    private Path getSessionDir(Path snapshotDir, Session session) {
        return Paths.get(snapshotDir.toString(), session.getSessionId().toString());
    }

    private void writeSessionMetadata(Path snapshotDir, Session session) throws IOException {
        Files.createDirectories(getSessionDir(snapshotDir, session));
        Path metadata = Paths.get(getSessionDir(snapshotDir, session).toString(), "metadata.json");
        Path tempFile = Files.createTempFile("metadata_", ".json");

        try (OutputStream os = new FileOutputStream(tempFile.toFile())) {
            IOUtils.copy(new ByteArrayInputStream(session.serialize().getBytes(StandardCharsets.UTF_8)), os);
            Files.move(tempFile, metadata, StandardCopyOption.REPLACE_EXISTING);
        }


    }

    public synchronized Optional<Session> getCurrentSession() {
        return Optional.ofNullable(sessions.isEmpty() ? null : sessions.peek());
    }

    public void writeImageToSession(Session session, InputStream is) throws IOException {
        Path writeDir = Files.createDirectories(Paths.get(snapshotFolder.toString(), session.getSessionId().toString()));
        Path photo = Files.createFile(Paths.get(writeDir.toString(), session.getPhotoCountTaken() + ".jpg"));

        try(OutputStream os = new FileOutputStream(photo.toFile())) {
            IOUtils.copy(is, os);
            session.mapImageWithCurrentId(session.getPhotoCountTaken() + ".jpg");
            writeSessionMetadata(snapshotFolder, session);
        }
    }

    public synchronized void dumpSessions() {
        log.info("Session dump start for session: {}, start: {}, end: {}",
                sessionStart.toString(), LocalDateTime.now(ZoneOffset.UTC), sessionId.toString());
        sessions.stream().forEach(p -> {
            log.info(p.toString());
        });
        log.info("Session dump done for session: {}", sessionId.toString());
    }

    public void cleanupIncompleteSessions() throws IOException {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(snapshotFolder)) {
            for (Path path : paths) {
                if (Files.isDirectory(path) && (Files.list(path).count() <= 0)) {
                    log.debug("Cleanung up: {}, file coun: {}", path.toString(), Files.list(path).count());
                    FileUtils.deleteDirectory(path.toFile());
                }
            }
        }
    }
}
