package com.diyphotobooth.lordbritishix.jobprocessor;

import com.diyphotobooth.lordbritishix.model.Session;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
        Session session = Session.builder().sessionId(UUID.randomUUID()).build();
        CountDownLatch latch = new CountDownLatch(1);
        JobProcessor fixture = new JobProcessor(snapshotFolder.toString(), ImmutableList.of(p -> {
            assertThat(session, is(p));
            latch.countDown();
        }));

        fixture.start();
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
            sessions.add(Session.builder().sessionId(UUID.randomUUID()).build());
        }

        CountDownLatch latch = new CountDownLatch(count * 2);
        JobProcessor fixture = new JobProcessor(snapshotFolder.toString(), ImmutableList.of(p -> {
            processedSessions1.add(p);
            latch.countDown();
        }, p -> {
            processedSessions2.add(p);
            latch.countDown();
        }));

        fixture.start();

        sessions.forEach(p -> fixture.queueSession(p));

        latch.await(1, TimeUnit.MINUTES);
        fixture.stop();

        assertThat(sessions, is(processedSessions1));
        assertThat(sessions, is(processedSessions2));
    }
}
