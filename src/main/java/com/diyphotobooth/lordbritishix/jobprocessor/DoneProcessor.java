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
public class DoneProcessor implements Consumer<Session> {
    private final SessionUtils sessionUtils;
    private final Path snapshotDir;

    @Inject
    public DoneProcessor(SessionUtils sessionUtils,
                            @Named("snapshot.folder") String snapshotDir) {
        this.sessionUtils = sessionUtils;
        this.snapshotDir = Paths.get(snapshotDir);
    }

    @Override
    public void accept(Session session) {
        if (session.getState() == Session.State.ERROR) {
            log.debug("Session is already at terminal state (ERROR), not setting to DONE: {} ", session.toString());
            return;
        }
        session.setState(Session.State.DONE);
        sessionUtils.updateSessionStateAndPersistQuietly(snapshotDir, session, Session.State.DONE);

        log.debug("Sending session to the DoneProcessor: {} ", session.toString());
    }
}
