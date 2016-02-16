package com.diyphotobooth.lordbritishix.jobprocessor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.diyphotobooth.lordbritishix.model.Session;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class JobProcessorTest {
    private Path snapshotFolder;

    @Before
    public void setup() throws IOException {
        snapshotFolder = Files.createTempDirectory("");
    }

    @After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(snapshotFolder.toFile());
    }

    @Test
    public void queueSessionProcessesQueuedSessionAfterAWhile() throws InterruptedException, ExecutionException {
        Session session = Session.builder().sessionId(UUID.randomUUID()).state(Session.State.DONE_TAKING_PHOTO).build();
        CountDownLatch latch = new CountDownLatch(1);
        JobProcessor fixture = new JobProcessor(snapshotFolder.toString());

        fixture.start(ImmutableList.of(p -> {
            assertThat(session, is(p));
            latch.countDown();
        }));

        fixture.queueSession(session);

        latch.await(1, TimeUnit.MINUTES);

        fixture.stop();
    }

    @Test
    public void queueManySessionProcessesAllQueuedSessions() throws InterruptedException, ExecutionException {
        List<Session> sessions = Lists.newArrayList();
        List<Session> processedSessions1 = Lists.newArrayList();
        List<Session> processedSessions2 = Lists.newArrayList();

        int count = 100;

        for (int x = 0; x < count; ++x) {
            sessions.add(Session.builder().sessionId(UUID.randomUUID()).state(Session.State.DONE_TAKING_PHOTO).build());
        }

        CountDownLatch latch = new CountDownLatch(count * 2);
        JobProcessor fixture = new JobProcessor(snapshotFolder.toString());

        fixture.start(ImmutableList.of(p -> {
            processedSessions1.add(p);
            latch.countDown();
        }, p -> {
            processedSessions2.add(p);
            latch.countDown();
        }));

        sessions.forEach(p -> fixture.queueSession(p));

        latch.await(1, TimeUnit.MINUTES);
        fixture.stop();

        assertThat(sessions, is(processedSessions1));
        assertThat(sessions, is(processedSessions2));
    }

    @Test
    public void getUnprocessedSessionsReturnsOnlyUnprocessedSessions() throws InterruptedException, ExecutionException, IOException {
        createSessions(5, snapshotFolder, Session.State.DONE_TAKING_PHOTO);
        createSessions(5, snapshotFolder, Session.State.PREPARING_MONTAGE);
        createSessions(5, snapshotFolder, Session.State.DONE);
        createSessions(5, snapshotFolder, Session.State.TAKING_PHOTO);
        createSessions(5, snapshotFolder, Session.State.ERROR);
        createSessions(5, snapshotFolder, Session.State.PRINTING);

        JobProcessor fixture = new JobProcessor(snapshotFolder.toString());

        List<Session> sessions = fixture.getUnprocessedSessionsFromDirectory(snapshotFolder);
        assertThat(sessions.size(), is(15));
        assertThat(sessions.stream().filter(p -> p.getState() == Session.State.PREPARING_MONTAGE).collect(Collectors.toList()).size(), is(5));
        assertThat(sessions.stream().filter(p -> p.getState() == Session.State.DONE_TAKING_PHOTO).collect(Collectors.toList()).size(), is(5));
        assertThat(sessions.stream().filter(p -> p.getState() == Session.State.PRINTING).collect(Collectors.toList()).size(), is(5));
    }

    @Test
    public void startProcessesUnprocessedSessions() throws InterruptedException, ExecutionException, IOException {
        int count = 5;
        List<Session> createdSessions = createSessions(count, snapshotFolder, Session.State.DONE_TAKING_PHOTO);
        List<Session> processedSessions = Lists.newArrayList();

        CountDownLatch latch = new CountDownLatch(count);
        JobProcessor fixture = new JobProcessor(snapshotFolder.toString());

        fixture.start(ImmutableList.of(p -> {
            latch.countDown();
            processedSessions.add(p);
        }));
        latch.await(1, TimeUnit.MINUTES);
        fixture.stop();
        assertThat(processedSessions.size(), is(5));
        assertThat(processedSessions.stream().map(p -> p.getSessionId().toString()).sorted().collect(Collectors.toList()),
                    is(createdSessions.stream().map(p -> p.getSessionId().toString()).sorted().collect(Collectors.toList())));
    }


    private List<Session> createSessions(int count, Path snapshotFolder, Session.State state) throws IOException {
        List<Session> createdSession = Lists.newArrayList();
        for (int x = 0; x < count; ++x) {
            Session session = Session.builder()
                    .sessionId(UUID.randomUUID())
                    .sessionDate(LocalDateTime.now())
                    .state(state)
                    .imageMap(Maps.newHashMap())
                    .build();
            Path sessionDir = Files.createDirectories(Paths.get(snapshotFolder.toString(), UUID.randomUUID().toString()));
            String json = session.toJson();
            Path jsonFile = Files.createFile(Paths.get(sessionDir.toString(), "metadata.json"));
            try (OutputStream os = new FileOutputStream(jsonFile.toFile())) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            createdSession.add(session);
        }

        return createdSession;
    }

}
