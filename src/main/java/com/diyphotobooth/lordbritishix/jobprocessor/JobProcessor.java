package com.diyphotobooth.lordbritishix.jobprocessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import org.apache.commons.io.FileUtils;
import com.diyphotobooth.lordbritishix.model.Session;
import com.diyphotobooth.lordbritishix.model.SessionUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Job Processor is responsible for:
 *
 * 1. Reading the session metadata.
 * 2. Creating a montage from a set of images based on the session metadata
 * 3. Sending the composed montage to the printer for printing
 *
 * Jobs are acquired via:
 * 1. Scanning the snapshotFolder for unprinted sessions
 * 2. Real-time, using queueJob
 *
 * Once the Job Processor is stopped, it can never be restarted again.
 */
@Slf4j
@Singleton
public class JobProcessor {
    private final Path snapshotFolder;
    private final BlockingQueue<Session> jobs = Queues.newLinkedBlockingDeque();
    private final JobProcessorStopper stopper;
    private final ExecutorService executorService;
    private final SessionUtils sessionUtils;

    @Inject
    public JobProcessor(
            @Named("snapshot.folder") String snapshotFolder,
            SessionUtils sessionUtils) {
        this.snapshotFolder = Paths.get(snapshotFolder);
        this.stopper = new JobProcessorStopper();
        this.executorService = Executors.newSingleThreadExecutor();
        this.sessionUtils = sessionUtils;
    }

    public void start(List<Consumer<Session>> processingPipeline) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Future<Void> future = executorService.submit(() -> {
            List<Session> previousUnprocessedSessions = getUnprocessedSessionsFromDirectory(snapshotFolder);

            //Reset to initial state so it can be reprocessed again cleanly
            previousUnprocessedSessions.stream().forEach(p -> {
                log.debug("Attempting to reprocess previous session: {}", p.toString());
                p.setState(Session.State.DONE_TAKING_PHOTO);
                sessionUtils.updateSessionStateAndPersistQuietly(snapshotFolder, p, Session.State.DONE_TAKING_PHOTO);
            });

            previousUnprocessedSessions.forEach(p -> log.debug("Attempting to reprocess previous session: {}", p.toString()));
            jobs.addAll(previousUnprocessedSessions);

            latch.countDown();
            log.debug("Starting Job Processor");
            while (!stopper.isStopped()) {
                try {
                    Session session = jobs.take();
                    for (Consumer<Session> processor : processingPipeline) {
                        processor.accept(session);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            log.debug("Job Processor complete");
            return null;
        });

        latch.await();

        stopper.setFuture(future);
    }

    @VisibleForTesting
    List<Session> getUnprocessedSessionsFromDirectory(Path snapshotDir) throws IOException {
        Iterator<File> files = FileUtils.iterateFiles(snapshotDir.toFile(), new String[]{"json"}, true);
        List<Session> unprocessedSessions = Lists.newArrayList();

        while (files.hasNext()) {
            File file = files.next();
            try {
                Session session = Session.fromJsonInFile(file);
                if (isSessionUnprocessed(session)) {
                    unprocessedSessions.add(session);
                }
            }
            catch(Exception e) {
                log.error("Unable to process the file: {}", file.toString(), e);
            }
        }

        return unprocessedSessions;
    }

    public void stop() throws ExecutionException, InterruptedException {
        stopper.stop();
        executorService.shutdown();
    }

    public void queueSession(Session session) {
        if (isSessionUnprocessed(session)) {
            log.debug("Sending session to the processor: {} ", session.toString());
            jobs.add(session);
        }
    }

    private boolean isSessionUnprocessed(Session session) {
        return
                //If we stop at the middle, we still want to be able to recover jobs that were in the middle of processing
                session.getState() == Session.State.DONE_TAKING_PHOTO ||
                session.getState() == Session.State.DONE_COMPOSING_MONTAGE ||
                session.getState() == Session.State.DONE_PRINTING ||

                //If it's in a retry state, let's do it again
                session.getState() == Session.State.RETRY;
    }

    @Data
    private class JobProcessorStopper {
        private Future<Void> future;
        private boolean isStopped = false;

        public void stop() throws ExecutionException, InterruptedException {
            isStopped = true;
            if (future != null) {
                future.cancel(true);
            }
        }
    }
}
