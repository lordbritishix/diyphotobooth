package com.diyphotobooth.lordbritishix.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import com.google.common.annotations.VisibleForTesting;

/**
 * Reads the mjpeg stream payloads using an iterator interface. The format of an mjpeg stream is:
 * > header
 * > payload
 * > header
 * > payload
 * ...
 */
public class MJpegStreamIterator implements Iterator<byte[]> {
    private InputStream stream;
    private final MJpegStreamHeader header;

    public MJpegStreamIterator(InputStream is) {
        header = new MJpegStreamHeader();
        resetHeader();

        stream = new BufferedInputStream(is);
    }

    private class MJpegStreamHeader {
        private int contentLength;
        private String boundary;
        private String contentType;
    }


    @VisibleForTesting
    String readLine() throws IOException {
        int ch;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((ch = stream.read()) != -1) {
            if (ch == '\r') {
                int ch1 = stream.read();

                if (ch1 == '\n') {
                    return baos.toString(StandardCharsets.UTF_8.toString());
                }

                baos.write(ch);
                baos.write(ch1);
            } else {
                baos.write(ch);
            }
        }

        return baos.size() <= 0 ? null : baos.toString(StandardCharsets.UTF_8.name());
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
            return stream.available() != 0;
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
            throw new IllegalStateException("End of stream reached");
        }

        MJpegStreamHeader streamHeader = null;
        try {
            streamHeader = readHeader();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int ch;
            while ((ch = stream.read()) != -1) {
                baos.write(ch);
                if (baos.size() >= streamHeader.contentLength) {
                    break;
                }
            }

            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }
}
