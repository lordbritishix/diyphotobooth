package com.diyphotobooth.lordbritishix.jobprocessor;

import com.diyphotobooth.lordbritishix.model.Session;
import com.diyphotobooth.lordbritishix.model.SessionUtils;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

@Slf4j
public class PrintProcessor implements Consumer<Session> {
    private final SessionUtils sessionUtils;
    private final Path snapshotDir;

    @Inject
    public PrintProcessor(SessionUtils sessionUtils,
                          @Named("snapshot.folder") String snapshotDir) {
        this.sessionUtils = sessionUtils;
        this.snapshotDir = Paths.get(snapshotDir);
    }

    @Override
    public void accept(Session session) {
        session.setState(Session.State.PRINTING);
        sessionUtils.updateSessionStateAndPersistQuietly(snapshotDir, session, Session.State.PRINTING);

        log.debug("Sending session to the PrintProcessor: {} ", session.toString());
    }
}
