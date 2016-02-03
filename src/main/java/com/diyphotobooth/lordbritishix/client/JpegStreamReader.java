package com.diyphotobooth.lordbritishix.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JpegStreamReader {
    private int frameLength;
    private String boundary;
    private InputStream stream;
    private int boundaryLength;
    public JpegStreamReader(InputStream is) {
        stream = new BufferedInputStream(is);
    }

    private String readLine() throws IOException {
        int ch;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((ch = stream.read()) != -1) {
            if (ch == '\r') {
                int ch1 = stream.read();

                if (ch1 == '\n') {
                    return baos.toString("UTF-8");
                }

                baos.write(ch);
                baos.write(ch1);
            }
            else {
                baos.write(ch);
            }
        }

        return baos.toString("UTF-8");
    }

    private void readHeader() throws IOException {
        boundary = readLine();
        boundaryLength = boundary.getBytes(StandardCharsets.UTF_8).length;
        readLine();
        frameLength = Integer.parseInt(readLine().split(":")[1].trim());
        readLine();
    }


    public byte[] nextFrame() throws IOException {
        readHeader();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int ch;
        while ((ch = stream.read()) != -1) {
            baos.write(ch);
            if (baos.size() >= frameLength) {
                break;
            }
        }

        return baos.toByteArray();
    }
}
