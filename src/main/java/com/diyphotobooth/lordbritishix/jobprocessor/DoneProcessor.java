package com.diyphotobooth.lordbritishix.jobprocessor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import com.diyphotobooth.lordbritishix.StatsCounter;
import com.diyphotobooth.lordbritishix.model.Session;
import com.diyphotobooth.lordbritishix.model.SessionUtils;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DoneProcessor implements Consumer<Session> {
    private final SessionUtils sessionUtils;
    private final Path snapshotDir;
    private final StatsCounter statsCounter;

    @Inject
    public DoneProcessor(SessionUtils sessionUtils,
                         @Named("snapshot.folder") String snapshotDir,
                         StatsCounter statsCounter) {
        this.sessionUtils = sessionUtils;
        this.snapshotDir = Paths.get(snapshotDir);
        this.statsCounter = statsCounter;
    }

    @Override
    public void accept(Session session) {
        log.debug("Sending session to the DoneProcessor: {} ", session.toString());

        if ((session.getState() == Session.State.ERROR) || (session.getState() == Session.State.DONE)) {
            log.debug("Session is already at terminal state (ERROR), not setting to DONE: {} ", session.toString());
            statsCounter.incrementSessionsThatEncounteredError();
            return;
        }

        if (session.getState() == Session.State.RETRY) {
            log.debug("Retry session by restarting the application: {} ", session.toString());
            statsCounter.incrementSessionsToBeRetried();
            return;
        }

        log.debug("Updating session to DONE: {} ", session.toString());
        sessionUtils.updateSessionStateAndPersistQuietly(snapshotDir, session, Session.State.DONE);
        statsCounter.incrementSessionsCompleted();
    }
}
