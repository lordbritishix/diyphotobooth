package com.diyphotobooth.lordbritishix.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.PeekingIterator;

import lombok.Data;

/**
 * Reads the mjpeg stream payloads using an iterator interface. The format of an mjpeg stream is:
 * > header
 * > payload
 * > header
 * > payload
 * ...
 */
public class MJpegStreamIterator implements Iterator<byte[]> {
    private PeekingIterator<byte[]> iterator;
    private InputStream stream;
    private final MJpegStreamHeader header;
    private final ByteArrayOutputStream charBaos = new ByteArrayOutputStream();
    private final ByteArrayOutputStream lineBaos = new ByteArrayOutputStream();

    public MJpegStreamIterator(InputStream is) {
        header = new MJpegStreamHeader();
        resetHeader();

        stream = new BufferedInputStream(is);
    }

    @Data
    private class MJpegStreamHeader {
        private int contentLength;
        private String boundary;
        private String contentType;
    }


    @VisibleForTesting
    String readLine() throws IOException {
        int ch;
        lineBaos.reset();
        while ((ch = stream.read()) != -1) {
            if (ch == '\r') {
                int ch1 = stream.read();

                if (ch1 == '\n') {
                    return lineBaos.toString(StandardCharsets.UTF_8.toString());
                }

                lineBaos.write(ch);
                lineBaos.write(ch1);
            } else {
                lineBaos.write(ch);
            }
        }

        return lineBaos.size() <= 0 ? null : lineBaos.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * MJPEG stream header is structued like this:
     * 1st line: Boundary
     * 2nd line: Content Type
     * 3rd line: Content Length
     * 4th line: New line
     *
     * This function only reads the content length - we don't care about the rest
     */
    private MJpegStreamHeader readHeader() throws IOException {
        resetHeader();
        readLine();
        header.boundary = readLine();
        header.contentType = getValue(readLine());
        header.contentLength = Integer.parseInt(getValue(readLine()));
        readLine();

        return header;
    }

    private void resetHeader() {
        header.boundary = null;
        header.contentLength = -1;
        header.contentType = null;
    }

    @Override
    public boolean hasNext() {
        try {
            stream.mark(1);
            int read = stream.read();
            stream.reset();
            return read != -1;
        } catch (IOException e) {
            return false;
        }
    }

    private String getValue(String string) {
        String[] pair = string.split(":");

        if (pair.length > 1) {
            return string.split(":")[1].trim();
        }
        else {
            return  string;
        }
    }

    @Override
    public byte[] next() {
        if (!hasNext()) {
            charBaos.reset();
            lineBaos.reset();
            throw new IllegalStateException("End of stream reached");
        }

        MJpegStreamHeader streamHeader = null;
        try {
            streamHeader = readHeader();

            charBaos.reset();

            int ch;
            while ((ch = stream.read()) != -1) {
                charBaos.write(ch);
                if (charBaos.size() >= streamHeader.contentLength) {
                    break;
                }
            }

            return charBaos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }
}
