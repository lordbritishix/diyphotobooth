package com.diyphotobooth.lordbritishix.jobprocessor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import javax.print.PrintException;
import com.diyphotobooth.lordbritishix.jobprocessor.printer.MontagePrinter;
import com.diyphotobooth.lordbritishix.model.Session;
import com.diyphotobooth.lordbritishix.model.SessionUtils;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrintProcessor implements Consumer<Session> {
    private final SessionUtils sessionUtils;
    private final Path snapshotDir;
    private final MontagePrinter printer;

    @Inject
    public PrintProcessor(SessionUtils sessionUtils,
                          @Named("snapshot.folder") String snapshotDir,
                          MontagePrinter printer) {
        this.sessionUtils = sessionUtils;
        this.snapshotDir = Paths.get(snapshotDir);
        this.printer = printer;
    }

    @Override
    public void accept(Session session) {
        log.debug("Sending session to the PrintProcessor: {} ", session.toString());

        if (session.getState() != Session.State.DONE_COMPOSING_MONTAGE) {
            log.error("PrintProcessor is unable to process this session: {}", session.toString());
            return;
        }

        try {
            printer.print(session.getMontage());
            sessionUtils.updateSessionStateAndPersistQuietly(snapshotDir, session, Session.State.DONE_PRINTING);
            log.info("PrintProcessor success in printing session: {}", session.toString());
        } catch (IOException e) {
            //Image was not found, let's recompose the montage again on the next try
            log.error("Unable to print the session: {} ", session.toString(), e);
            sessionUtils.updateSessionStateAndPersistQuietly(snapshotDir, session, Session.State.RETRY);
        } catch (PrintException e) {
            //Printing error - retry again on the next round
            log.error("Unable to print the session: {} ", session.toString(), e);
            sessionUtils.updateSessionStateAndPersistQuietly(snapshotDir, session, Session.State.RETRY);
        } catch (Exception e) {
            //Might be an unrecoverable error - don't retry
            log.error("Unable to print the session: {} ", session.toString(), e);
            sessionUtils.updateSessionStateAndPersistQuietly(snapshotDir, session, Session.State.ERROR);
        }
    }
}
