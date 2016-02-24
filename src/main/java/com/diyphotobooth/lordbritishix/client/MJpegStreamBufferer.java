package com.diyphotobooth.lordbritishix.client;

import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.google.common.collect.Queues;

/**
 * Buffers MJpeg streams. If the buffered streams have reached capacity, the incoming stream is discarded.
 * Once stopped, the buffer cannot be restarted.
 */
public class MJpegStreamBufferer {
    private final Deque<byte[]> buffer;
    private final ReadWriteLock lock;
    private final int bufferSize;
    private volatile boolean started = false;
    private volatile boolean stopped = false;

    public MJpegStreamBufferer(int bufferSize) {
        this.lock = new ReentrantReadWriteLock(true);
        this.bufferSize = bufferSize;
        this.buffer = Queues.newArrayDeque();
    }

    /**
     * Starts reading the stream on a separate thread and places it in a buffer. If the buffer is full,
     * the incoming streams are discarded.
     *
     * If we are at the end of the stream or if the stopAndAwait method has been called, the read stops.
     */
    public boolean start(MJpegStreamBufferListener listener, MJpegStreamIterator iterator) {
        if (started) {
            return false;
        }

        CompletableFuture.runAsync(() -> {
            while(!stopped) {
                Lock writeLock = lock.writeLock();
                try {
                    writeLock.lock();
                    if (buffer.size() < bufferSize) {
                        byte[] data = iterator.next();

                        if (data != null) {
                            buffer.add(data);
                            listener.streamBuffered(data);
                        }
                    }
                    else {
                        byte[] data = iterator.next();
                        byte[] discarded = buffer.pop();
                        buffer.add(data);
                        listener.streamDiscarded(discarded);
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

        started = true;

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

            if (!buffer.isEmpty()) {
                return buffer.pop();
            }

            return null;
        }
        finally {
            readLock.unlock();
        }
    }
}
