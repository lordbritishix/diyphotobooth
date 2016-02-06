package com.diyphotobooth.lordbritishix.client;

import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.google.common.collect.Queues;

/**
 * Buffers MJpeg streams. If the buffered streams have reached capacity, the incoming stream is discarded.
 * Once stopped, the buffer cannot be restarted.
 */
public class MJpegStreamBufferer {
    private final MJpegStreamIterator iterator;
    private final Deque<byte[]> buffer;
    private final ExecutorService executorService;
    private final ReadWriteLock lock;
    private CompletableFuture future;
    private final int bufferSize;
    private volatile boolean stopped = false;

    public MJpegStreamBufferer(MJpegStreamIterator iterator, int bufferSize) {
        this.iterator = iterator;
        this.executorService = Executors.newSingleThreadExecutor();
        this.lock = new ReentrantReadWriteLock();
        this.bufferSize = bufferSize;
        this.buffer = Queues.newArrayDeque();
    }

    /**
     * Starts reading the stream on a separate thread and places it in a buffer. If the buffer is full,
     * the incoming streams are discarded.
     *
     * If we are at the end of the stream or if the stop method has been called, the read stops.
     */
    public boolean start(MJpegStreamBufferListener listener) {
        if (future != null || stopped) {
            return false;
        }

        future = CompletableFuture.runAsync(() -> {
            while(iterator.hasNext() && !stopped) {
                if (executorService.isShutdown()) {
                    break;
                }

                Lock writeLock = lock.writeLock();
                try {
                    writeLock.lock();
                    if (buffer.size() < bufferSize) {
                        byte[] data = iterator.next();
                        buffer.add(data);
                        listener.streamBuffered(data);
                    }
                    else {
                        listener.streamDiscarded(iterator.next());
                    }
                }
                catch(Exception e) {
                    break;
                }
                finally {
                    writeLock.unlock();
                }
            }

            stopped = true;
            listener.stopped();
        });

        return true;
    }

    /**
     * Stops the buffering of the stream. Calls MJpegStreamBufferListener#stopped once the thread is complete.
     */
    public void stop() {
        stopped = true;
    }

    /**
     * Returns true if there is no data in the buffer. This does not necessarily mean that the buffer is stopped
     */
    public boolean hasData() {
        return buffer.size() > 0;
    }

    /**
     * Returns true if stopped
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
     * Gets a "stream element" from the buffer. If the buffer is empty, null is returned.
     */
    public byte[] get() {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            return buffer.pollFirst();
        }
        finally {
            readLock.unlock();
        }
    }
}
