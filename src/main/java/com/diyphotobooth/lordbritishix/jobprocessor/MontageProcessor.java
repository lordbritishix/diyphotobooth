package com.diyphotobooth.lordbritishix.jobprocessor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import com.diyphotobooth.lordbritishix.jobprocessor.montage.MontageMaker;
import com.diyphotobooth.lordbritishix.model.Session;
import com.diyphotobooth.lordbritishix.model.SessionUtils;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MontageProcessor implements Consumer<Session> {
    private final SessionUtils sessionUtils;
    private final Path snapshotDir;
    private final MontageMaker montageMaker;

    @Inject
    public MontageProcessor(SessionUtils sessionUtils,
                            @Named("snapshot.folder") String snapshotDir,
                            @Named("singlemontage") MontageMaker montageMaker) {
        this.sessionUtils = sessionUtils;
        this.snapshotDir = Paths.get(snapshotDir);
        this.montageMaker = montageMaker;
    }

    @Override
    public void accept(Session session) {
        log.debug("Session being processed by the MontageProcessor: {} ", session.toString());

        if (!isSessionValid(session)) {
            log.error("MontageProcessor is unable to process this session: {}", session.toString());
            return;
        }

        Path montage = montageMaker.apply(session, sessionUtils.getSessionDir(snapshotDir, session));

        if (montage != null) {
            session.setMontage(montage);
            sessionUtils.updateSessionStateAndPersistQuietly(snapshotDir, session, Session.State.DONE_COMPOSING_MONTAGE);
            log.debug("MontageProcessor success for session: {} ", session.toString());
        }
        else {
            log.error("Unable to compose the montage for session: {}", session.toString());
            //Probably a permanent error - so set to error so we don't reprocess it again
            sessionUtils.updateSessionStateAndPersistQuietly(snapshotDir, session, Session.State.ERROR);
        }
    }

    private boolean isSessionValid(Session session) {
        return (session.getState() == Session.State.DONE_TAKING_PHOTO) &&
                (session.getNumberOfPhotosAlreadyTaken() == session.getNumberOfPhotosToBeTaken()) &&
                (session.getNumberOfPhotosAlreadyTaken() == session.getImageMap().size());
    }
}
