package com.diyphotobooth.lordbritishix.controller;

import com.diyphotobooth.lordbritishix.client.IpCameraException;
import com.diyphotobooth.lordbritishix.client.IpCameraHttpClient;
import com.diyphotobooth.lordbritishix.client.MJpegStreamBufferer;
import com.diyphotobooth.lordbritishix.model.Session;
import com.diyphotobooth.lordbritishix.model.SessionManager;
import com.diyphotobooth.lordbritishix.model.Template;
import com.diyphotobooth.lordbritishix.utils.StageManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
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
    private SessionManager sessionManager;

    @Mock
    private MJpegStreamBufferer bufferer;

    private CameraSceneController fixture;

    @Before
    public void setup() {
        fixture = new CameraSceneController(stageManager, client, 5, 1, template, sessionManager);
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

    @Test
    public void startSessionCompletesSessionOnHappyCase() throws InterruptedException, IOException {
        int count = 10;
        AtomicInteger counter = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(count);
        when(sessionManager.getOrCreateNewSession(anyInt(), anyBoolean(), any(Template.class))).thenReturn(new Session(count, null));

        fixture.startSession(count, 0, 0, p -> {
            assertTrue(p != null);
            counter.incrementAndGet();
            assertThat(p.getPhotoCountTaken(), is(counter.get()));
            latch.countDown();
        });

        assertTrue(latch.await(15, TimeUnit.SECONDS));
        assertThat(counter.get(), is(count));
    }

    @Test
    public void startSessionCompletesSessionOnZeroPhotos() throws InterruptedException, IOException {
        int count = 0;
        AtomicInteger counter = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(1);
        when(sessionManager.getOrCreateNewSession(anyInt(), anyBoolean(), any(Template.class))).thenReturn(new Session(count, null));

        fixture.startSession(count, 0, 0, p -> {
            counter.incrementAndGet();
        }).whenComplete((p, q) -> {
            latch.countDown();
        });

        assertTrue(latch.await(15, TimeUnit.SECONDS));
        assertThat(counter.get(), is(count));
    }


}
