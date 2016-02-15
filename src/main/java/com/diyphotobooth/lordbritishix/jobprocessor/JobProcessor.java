package com.diyphotobooth.lordbritishix.jobprocessor;

import com.diyphotobooth.lordbritishix.model.Session;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

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
 * Once the Job Processor is stopped, it can never be restarted again
 */
@Slf4j
@Singleton
public class JobProcessor {
    private final Path snapshotFolder;
    private final BlockingQueue<Session> jobs = Queues.newLinkedBlockingDeque();
    private final JobProcessorStopper stopper;
    private final ExecutorService executorService;

    @Inject
    public JobProcessor(@Named("snapshot.folder") String snapshotFolder) {
        this.snapshotFolder = Paths.get(snapshotFolder);
        this.stopper = new JobProcessorStopper();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void start(List<Consumer<Session>> processingPipeline) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Future<Void> future = executorService.submit(() -> {
            List<Session> previousUnprocessedSessions = getUnprocessedSessionsFromDirectory(snapshotFolder);
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
            Session session = Session.fromJsonInFile(files.next());
            if (isSessionUnprocessed(session)) {
                unprocessedSessions.add(session);
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
        return session.getState() == Session.State.DONE_TAKING_PHOTO ||
                session.getState() == Session.State.PREPARING_MONTAGE ||
                session.getState() == Session.State.PRINTING;
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
