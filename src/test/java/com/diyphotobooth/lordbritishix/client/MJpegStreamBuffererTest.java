package com.diyphotobooth.lordbritishix.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import com.diyphotobooth.lordbritishix.TestUtils;
import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;

public class MJpegStreamBuffererTest {
    @Test
    public void bufferDiscardsStreamWhenFull() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Pair<InputStream, List<byte[]>> stream = TestUtils.generateStream(100);

        MJpegStreamBufferer buffer = new MJpegStreamBufferer(5);

        buffer.start(new MJpegStreamBufferListener() {
            @Override
            public void stopped() {
                latch.countDown();
            }

            @Override
            public void streamBuffered(byte[] stream) {

            }

            @Override
            public void streamDiscarded(byte[] stream) {
            }
        }, new MJpegStreamIterator(stream.getLeft()));

        latch.await();

        for (int x = 0; x < 5; ++x) {
            assertThat(buffer.get(), is(stream.getRight().get(x)));
        }

        assertNull(buffer.get());
    }

    @Test
    public void bufferStopsWhenStopped() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Pair<InputStream, List<byte[]>> stream = TestUtils.generateStream(100);

        MJpegStreamBufferer buffer = new MJpegStreamBufferer(5);

        buffer.start(new MJpegStreamBufferListener() {
            @Override
            public void stopped() {
                latch.countDown();
            }

            @Override
            public void streamBuffered(byte[] stream) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void streamDiscarded(byte[] stream) {
            }
        }, new MJpegStreamIterator(stream.getLeft()));

        buffer.stop();
        latch.await();
    }

    @Test
    public void bufferBuffersAllData() throws IOException, InterruptedException {
        CountDownLatch stopLatch = new CountDownLatch(1);
        int size = 100;

        Pair<InputStream, List<byte[]>> stream = TestUtils.generateStream(size);

        MJpegStreamBufferer buffer = new MJpegStreamBufferer(200);

        buffer.start(new MJpegStreamBufferListener() {
            @Override
            public void stopped() {
                stopLatch.countDown();
            }

            @Override
            public void streamBuffered(byte[] stream) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void streamDiscarded(byte[] stream) {
            }
        }, new MJpegStreamIterator(stream.getLeft()));

        List<byte[]> streams = Lists.newArrayList();
        CountDownLatch readLatchGet = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            while(true) {
                if (buffer.isStopped()) {
                    break;
                }

                byte[] data = buffer.get();

                if (data != null) {
                    streams.add(data);
                }
            }

            readLatchGet.countDown();
        });

        t.start();

        readLatchGet.await();
        assertThat(streams.size(), is(size));

        for (int x = 0; x < size; ++x) {
            assertThat(streams.get(x), is(stream.getRight().get(x)));
        }

        stopLatch.await();
    }

}

