package com.diyphotobooth.lordbritishix.jobprocessor;

import com.diyphotobooth.lordbritishix.jobprocessor.montage.DefaultMontageMaker;
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
    private final DefaultMontageMaker montageMaker;

    @Inject
    public MontageProcessor(SessionUtils sessionUtils,
                            @Named("snapshot.folder") String snapshotDir,
                            DefaultMontageMaker montageMaker) {
        this.sessionUtils = sessionUtils;
        this.snapshotDir = Paths.get(snapshotDir);
        this.montageMaker = montageMaker;
    }

    @Override
    public void accept(Session session) {
        if (!isSessionValid(session)) {
            log.error("MontageProcessor is unable to process this session: {}", session.toString());
            return;
        }

        Path montage = montageMaker.apply(session, sessionUtils.getSessionDir(snapshotDir, session));

        if (montage != null) {
            session.setMontage(montage);
            sessionUtils.updateSessionStateAndPersistQuietly(snapshotDir, session, Session.State.PREPARING_MONTAGE);
        }
        else {
            log.error("Unable to compose the montage for session: {}", session.toString());
            //Probably a permanent error - so set to error so we don't reprocess it again
            sessionUtils.updateSessionStateAndPersistQuietly(snapshotDir, session, Session.State.ERROR);
        }

        log.debug("Session being processed by the MontageProcessor: {} ", session.toString());
    }

    private boolean isSessionValid(Session session) {
        return (session.getState() == Session.State.DONE_TAKING_PHOTO) &&
                (session.getNumberOfPhotosAlreadyTaken() == session.getNumberOfPhotosToBeTaken()) &&
                (session.getNumberOfPhotosAlreadyTaken() == session.getImageMap().size());
    }
}
