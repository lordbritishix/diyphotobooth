package com.diyphotobooth.lordbritishix.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.diyphotobooth.lordbritishix.jobprocessor.JobProcessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import com.diyphotobooth.lordbritishix.client.IpCameraException;
import com.diyphotobooth.lordbritishix.client.IpCameraHttpClient;
import com.diyphotobooth.lordbritishix.client.MJpegStreamBufferer;
import com.diyphotobooth.lordbritishix.model.SessionUtils;
import com.diyphotobooth.lordbritishix.model.Template;
import com.diyphotobooth.lordbritishix.utils.StageManager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CameraSceneControllerTest {
    @Mock
    private IpCameraHttpClient client;

    @Mock
    private StageManager stageManager;

    @Mock
    private Template template;

    @Mock
    private MJpegStreamBufferer bufferer;

    @Mock
    private SessionUtils sessionUtils;

    @Mock
    private JobProcessor jobProcessor;

    private CameraSceneController fixture;

    @Before
    public void setup() {
        fixture = new CameraSceneController(stageManager, client, 5, 1, template, "", sessionUtils, jobProcessor);
    }

    @Test
    public void startViewfinderStartsViewFinderAndCallsCallbackWhenStoppedWithoutErrors()
            throws IOException, IpCameraException, InterruptedException {
        when(bufferer.isStopped()).thenReturn(false).thenReturn(false).thenReturn(true);
        when(bufferer.get()).thenReturn("abc".getBytes(StandardCharsets.UTF_8));
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger dataCount = new AtomicInteger();
        AtomicInteger startedCount = new AtomicInteger();

        fixture.startViewfinder(p -> {
            startedCount.incrementAndGet();
        },
        q -> {
            assertNull(q);
            latch.countDown();
        },
        r -> {
            dataCount.incrementAndGet();
            assertThat(r, is("abc".getBytes(StandardCharsets.UTF_8)));
        },
        bufferer);

        assertThat(latch.await(15, TimeUnit.SECONDS), is(true));
        assertThat(dataCount.get(), is(2));
        assertThat(startedCount.get(), is(1));
    }

    @Test
    public void startViewfinderCallsStopCallbackWhenStoppedWithErrors1()
            throws IOException, IpCameraException, InterruptedException {
        when(client.getStream()).thenThrow(new RuntimeException());
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger dataCount = new AtomicInteger();
        AtomicInteger startedCount = new AtomicInteger();

        fixture.startViewfinder(p -> {
                    startedCount.incrementAndGet();
                },
                q -> {
                    assertTrue(q != null);
                    latch.countDown();
                },
                r -> {
                    dataCount.incrementAndGet();
                    assertThat(r, is("abc".getBytes(StandardCharsets.UTF_8)));
                },
                bufferer);

        assertThat(latch.await(15, TimeUnit.SECONDS), is(true));
        assertThat(dataCount.get(), is(0));
        assertThat(startedCount.get(), is(0));
    }

    @Test
    public void startViewfinderCallsStopCallbackWhenStoppedWithErrors2()
            throws IOException, IpCameraException, InterruptedException {
        when(bufferer.isStopped()).thenThrow(new RuntimeException());
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger dataCount = new AtomicInteger();
        AtomicInteger startedCount = new AtomicInteger();

        fixture.startViewfinder(p -> {
                    startedCount.incrementAndGet();
                },
                q -> {
                    assertTrue(q != null);
                    latch.countDown();
                },
                r -> {
                    dataCount.incrementAndGet();
                    assertThat(r, is("abc".getBytes(StandardCharsets.UTF_8)));
                },
                bufferer);

        assertThat(latch.await(15, TimeUnit.SECONDS), is(true));
        assertThat(dataCount.get(), is(0));
        assertThat(startedCount.get(), is(1));
    }

    //TODO: Write queueSessionProcessesQueuedSessionAfterAWhile for startSession
}
