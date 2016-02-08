package com.diyphotobooth.lordbritishix.controller;

import com.diyphotobooth.lordbritishix.client.IpCameraException;
import com.diyphotobooth.lordbritishix.client.IpCameraHttpClient;
import com.diyphotobooth.lordbritishix.client.MJpegStreamBufferListener;
import com.diyphotobooth.lordbritishix.client.MJpegStreamBufferer;
import com.diyphotobooth.lordbritishix.client.MJpegStreamIterator;
import com.diyphotobooth.lordbritishix.model.Session;
import com.diyphotobooth.lordbritishix.model.SessionManager;
import com.diyphotobooth.lordbritishix.model.Template;
import com.diyphotobooth.lordbritishix.scene.CameraScene;
import com.diyphotobooth.lordbritishix.scene.IdleScene;
import com.diyphotobooth.lordbritishix.utils.StageManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Controls the Camera Scene
 */
@Slf4j
public class CameraSceneController extends BaseController implements MJpegStreamBufferListener {
    private static final AudioClip SUCCESS = new AudioClip(CameraSceneController.class.getResource("/sound/success.wav").toString());
    private static final AudioClip REFRESH = new AudioClip(CameraSceneController.class.getResource("/sound/refresh.wav").toString());
    private static final AudioClip ERROR = new AudioClip(CameraSceneController.class.getResource("/sound/error.wav").toString());

    /**
     * State machine
     *
     * Stopped -----------> Streaming ------------> Session Start --------------> Session End
     *    ^                    |                          |                            ^
     *    |____________________|                          |____________________________|
     *
     */
    private enum State {
        STOPPED,
        STREAMING,
        SESSION_START,
        SESSION_END
    }

    private final IpCameraHttpClient client;
    private final int bufferSize;
    private final int countdownLengthInSeconds;
    private final Template template;
    private volatile State state;
    private volatile Optional<ViewFinderStopper> viewFinderStopper;
    private final AtomicLong discardedCount = new AtomicLong();
    private final SessionManager sessionManager;

    @Inject
    public CameraSceneController(StageManager stageManager,
                                 IpCameraHttpClient client,
                                 @Named("buffer.size") int bufferSize,
                                 @Named("countdown.length.sec") int countdownLengthInSeconds,
                                 Template template,
                                 SessionManager sessionManager) {
        super(stageManager);
        this.client = client;
        this.state = State.STOPPED;
        this.bufferSize = bufferSize;
        this.countdownLengthInSeconds = countdownLengthInSeconds;
        this.template = template;
        this.sessionManager = sessionManager;
        this.viewFinderStopper = Optional.empty();
    }

    @Override
    public void stopped() {
    }

    @Override
    public void streamBuffered(byte[] stream) {
    }

    @Override
    public void streamDiscarded(byte[] stream) {
        discardedCount.addAndGet(1);
        log.debug("Stream discarded: {}", discardedCount.get());
    }

    @Override
    public void handle(Node node, MouseEvent e) {
        if (state != State.STOPPED) {
            return;
        }

        viewFinderStopper = Optional.ofNullable(init());
    }

    private ViewFinderStopper init() {
        if (state != State.STOPPED) {
            throw new IllegalStateException("Unable to init from a non-stopped state");
        }

        return stoppedToStreaming();
    }

    /**
     * Transitions Stopped state to Streaming state
     */
    private ViewFinderStopper stoppedToStreaming() {
        if (state != State.STOPPED) {
            return null;
        }

        log.info("Transitioning from Stopped -> Streaming");

        ((CameraScene) getScene()).showLoading();
        return startViewfinder(
            //On viewfinder started
            onViewFinderStarted -> {
                log.info("Viewfinder ready");
                state = State.STREAMING;
                //After the viewfinder starts, start the session
                streamingToSessionStart()
                    //Once the session completes without without error, end the session without error
                    .thenRun(() -> {
                        try {
                            sessionStartToSessionEnd(false);
                        } catch (Exception e) {
                            log.error("Unable to transition from SessionStart to SessionEnd", e);
                        }
                    })
                    //Once the session ends with error, end the session with error
                    .exceptionally(p -> {
                        try {
                            log.error("Unable to perform SessionStart", p);
                            sessionStartToSessionEnd(true);
                        } catch (Exception e) {
                            log.error("Unable to transition from SessionStart to SessionEnd", e);
                        }
                        return null;
                    });
            },
            //On viewfinder stopped
            onViewFinderStopped -> {
                log.info("Viewfinder stopped");
                try {
                    streamingToStopped(onViewFinderStopped == null);
                } catch (Exception e) {
                    log.error("", e);
                    throw new RuntimeException(e);
                }
            },
            //Data has arrived from the viewfinder
            data -> {
                CameraScene scene = (CameraScene) getScene();
                scene.setCameraImage(new ByteArrayInputStream(data));
            },
            new MJpegStreamBufferer(bufferSize));
    }

    /**
     * Transitions Streaming state to Stopped state
     */
    private void streamingToStopped(boolean waitForCompletion) throws ExecutionException, InterruptedException {
        if ((state != State.STREAMING) && (state != State.STOPPED)) {
            return;
        }

        log.info("Transitioning from Streaming -> Stopped");

        if (viewFinderStopper.isPresent()) {
            viewFinderStopper.get().stopAndAwait(waitForCompletion);
        }

        state = State.STOPPED;

        Platform.runLater(() -> {
            REFRESH.play();
            ((CameraScene) getScene()).showRetry();
        });
    }

    private void sessionStartToSessionEnd(boolean isFailure) throws ExecutionException, InterruptedException {
        log.info("Transitioning from SessionStart -> SessionEnd");

        if (viewFinderStopper.isPresent()) {
            viewFinderStopper.get().stopAndAwait(true);
        }
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

        Platform.runLater(() -> {
            ((CameraScene) getScene()).clearCounterValue();

            if (isFailure) {
                ((CameraScene) getScene()).showWrong();
                ERROR.play();
                service.schedule(() -> Platform.runLater(() ->
                        getStageManager().showScene(IdleScene.class)), 5, TimeUnit.SECONDS);

            }
            else {
                ((CameraScene) getScene()).showCheckmark();
                SUCCESS.play();
                service.schedule(() -> Platform.runLater(() ->
                    getStageManager().showScene(IdleScene.class)), 5, TimeUnit.SECONDS);
            }

            service.shutdown();
        });
    }

    private CompletableFuture<Session> streamingToSessionStart() {
        log.info("Transitioning from Streaming -> SessionStart");

        if (state != State.STREAMING) {
            return CompletableFuture.completedFuture(null);
        }

        Platform.runLater(() -> {
            ((CameraScene) getScene()).setCountdownText("Ready?");
            ((CameraScene) getScene()).setCounterValue(1, template.getPhotoCount());
        });

        state = State.SESSION_START;
        return startSession(
                template.getPhotoCount(),
                countdownLengthInSeconds,
                1,
                (session) -> {
                    try(InputStream is = client.takePhoto(false)) {
                        sessionManager.writeImageToSession(session, is);
                        if (!session.isSessionFinished()) {
                            Platform.runLater(() -> ((CameraScene) getScene()).setCounterValue(
                                    session.getPhotoCountTaken() + 1, template.getPhotoCount()));
                        }
                    } catch (IpCameraException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    @Override
    public void sceneLoaded() {
        viewFinderStopper = Optional.ofNullable(init());
    }

    @Override
    public void shutdown() throws Exception {
        log.info("Shutting down CameraSceneController");

        if (viewFinderStopper.isPresent()) {
            viewFinderStopper.get().stopAndAwait(true);
        }

        sessionManager.getCurrentSession().ifPresent(p -> p.forceCompleteSession());
        log.info("CameraSceneController shutdown complete");
    }

    /**
     * Starts a session and walks the session through until it is complete.
     *
     * photoCount is the number of photos that will be taken for a given session
     * delayPerStepInSeconds is the number of seconds before the next photo is taken
     * startDelayInSeconds is the number of seconds elapsed before the first photo is taken
     */
    @VisibleForTesting
    CompletableFuture<Session> startSession(int photoCount, int delayPerStepInSeconds,
                                                    int startDelayInSeconds, Consumer<Session> sessionStepAction) {
        return CompletableFuture.supplyAsync(() -> {
            Session session;
            try {
                session = sessionManager.getOrCreateNewSession(photoCount, true, template);
                log.info("Session started: {}", session.getSessionId());
            } catch (Exception e) {
                throw new RuntimeException("Unable to start the session", e);
            }

            while (!session.isSessionFinished()) {
                CountDownLatch latch = new CountDownLatch(1);
                AtomicBoolean hasErrors = new AtomicBoolean();

                Platform.runLater(() -> ((CameraScene)getScene()).setCountdownValueAndStart(
                    delayPerStepInSeconds, startDelayInSeconds, q -> {
                        try {
                            log.info("Doing session: {} for id {}", session.getPhotoCountTaken(), session.getSessionId());
                            session.nextPhoto();
                            sessionStepAction.accept(session);
                            latch.countDown();
                        }
                        catch(Exception e) {
                            hasErrors.set(true);
                            session.forceCompleteSession();
                            latch.countDown();
                        }
                }));

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    //swallow
                }

                if (hasErrors.get()) {
                    throw new RuntimeException("Session was forced to complete - Something wrong has occured");
                }
            }

            return session;
        });
    }

    private void sleepQuietly(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts the view finder. Uses the passed bufferer to read data from the buffer.
     *
     * Informs viewFinderStartedCallback when the server has started to send images to the client
     * Informs viewFinderStoppedCallback when the image stream has stopped or an exception has occured while processing the stream
     * Informs dataReady when an image is available.
     *
     * @return a ViewFinderStopper that can be used to stop the view finder.
     */
    @VisibleForTesting
    ViewFinderStopper startViewfinder(Consumer viewFinderStartedCallback,
                                    Consumer<Throwable> viewFinderStoppedCallback,
                                    Consumer<byte[]> dataReady,
                                    MJpegStreamBufferer bufferer) {
        ViewFinderStopper poisonPill = new ViewFinderStopper();
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            log.info("Starting viewfinder");
            try(InputStream is = client.getStream()) {
                //Start stream producer
                bufferer.start(this, new MJpegStreamIterator(is));

                viewFinderStartedCallback.accept(null);

                //Start stream consumer
                while (!bufferer.isStopped() && !poisonPill.isStopped()) {
                    byte[] data = bufferer.get();
                    if (data != null) {
                        dataReady.accept(data);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).whenComplete((p, q) -> viewFinderStoppedCallback.accept(q));

        poisonPill.future = future;
        return poisonPill;
    }

    @Data
    private class ViewFinderStopper {
        private CompletableFuture future;
        private boolean stopped;

        public boolean isStopped() {
            return stopped;
        }

        public void stopAndAwait(boolean waitForCompletion) throws ExecutionException, InterruptedException {
            if (!isStopped()) {
                stopped = true;

                if (waitForCompletion) {
                    future.get();
                }
            }
        }
    }
}
