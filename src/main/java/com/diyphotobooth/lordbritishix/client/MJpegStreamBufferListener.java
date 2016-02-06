package com.diyphotobooth.lordbritishix.client;

/**
 * Interface to listen to the stream buffer events
 */
public interface MJpegStreamBufferListener {
    /**
     * Called when the stream buffer is stopped (either called explicitly or the underlying stream is closed)
     */
    void stopped();

    /**
     * Called when a stream has been buffered
     */
    void streamBuffered(byte[] stream);

    /**
     * Called when a stream has been discarded
     */
    void streamDiscarded(byte[] stream);
}
