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
public class MontageProcessor implements Consumer<Session> {
    private final SessionUtils sessionUtils;
    private final Path snapshotDir;

    @Inject
    public MontageProcessor(SessionUtils sessionUtils,
                            @Named("snapshot.folder") String snapshotDir) {
        this.sessionUtils = sessionUtils;
        this.snapshotDir = Paths.get(snapshotDir);
    }

    @Override
    public void accept(Session session) {
        session.setState(Session.State.PREPARING_MONTAGE);
        sessionUtils.updateSessionStateAndPersistQuietly(snapshotDir, session, Session.State.PREPARING_MONTAGE);

        log.debug("Sending session to the MontageProcessor: {} ", session.toString());
    }
}
