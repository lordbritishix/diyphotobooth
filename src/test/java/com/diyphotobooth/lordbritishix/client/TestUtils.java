package com.diyphotobooth.lordbritishix.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.collect.Lists;

public class TestUtils {
    public static InputStream linesOfStringsToInputStream(List<String> stringList) {
        String catString = stringList.stream().collect(Collectors.joining("\r\n"));
        return new ByteArrayInputStream(catString.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] createPayload(String payload) {
        int payloadLength = payload.getBytes(StandardCharsets.UTF_8).length;
        StringBuffer header = new StringBuffer();
        header.append("boundary\r\n");
        header.append("Content-Type: image/jpeg\r\n");
        header.append("Content-Length: ");
        header.append(payloadLength);
        header.append("\r\n");
        header.append("\r\n");
        header.append(payload);

        return header.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static Pair<InputStream, List<byte[]>> generateStream(int count) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<byte[]> payloads = Lists.newArrayList();
        for (int x = 0; x < count; ++x) {
            String payload = RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(1, 100));
            payloads.add(payload.getBytes(StandardCharsets.UTF_8));
            baos.write(createPayload(payload));
        }

        return Pair.of(new ByteArrayInputStream(baos.toByteArray()), payloads);
    }
}
