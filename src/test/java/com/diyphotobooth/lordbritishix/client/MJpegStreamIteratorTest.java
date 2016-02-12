package com.diyphotobooth.lordbritishix.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import com.diyphotobooth.lordbritishix.TestUtils;
import com.google.common.collect.ImmutableList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;

public class MJpegStreamIteratorTest {
    @Test
    public void readLineReturnsCorrectLine() throws IOException {
        InputStream input = TestUtils.linesOfStringsToInputStream(ImmutableList.of("a", "b\n", "c"));
        MJpegStreamIterator iterator = new MJpegStreamIterator(input);

        assertThat(iterator.readLine(), is("a"));
        assertThat(iterator.readLine(), is("b\n"));
        assertThat(iterator.readLine(), is("c"));
        assertNull(iterator.readLine());
    }

    @Test
    public void readLineReturnsCorrectLineForEmptyString() throws IOException {
        InputStream input = TestUtils.linesOfStringsToInputStream(ImmutableList.of(""));
        MJpegStreamIterator iterator = new MJpegStreamIterator(input);

        assertNull(iterator.readLine());
    }

    @Test
    public void nextShouldReturnNextLine() throws IOException {
        int count = 100;
        Pair<InputStream, List<byte[]>> payloads = TestUtils.generateStream(count);
        MJpegStreamIterator iterator = new MJpegStreamIterator(payloads.getLeft());

        int ctr = 0;
        while (iterator.hasNext()) {
            assertThat(iterator.next(), is(payloads.getRight().get(ctr)));
            ctr++;
        }

        assertThat(ctr, is(count));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test(expected = IllegalStateException.class)
    public void nextOnEndOfStreamShouldThrowExceptionWhenNext() throws IOException {
        Pair<InputStream, List<byte[]>> payloads = TestUtils.generateStream(1);
        MJpegStreamIterator iterator = new MJpegStreamIterator(payloads.getLeft());

        while (iterator.hasNext()) {
            iterator.next();
        }

        iterator.next();
    }

    @Test(expected = IllegalStateException.class)
    public void nextOnEmptyStreamShouldThrowExceptionWhenNext() throws IOException {
        Pair<InputStream, List<byte[]>> payloads = TestUtils.generateStream(0);
        MJpegStreamIterator iterator = new MJpegStreamIterator(payloads.getLeft());

        assertThat(iterator.hasNext(), is(false));
        iterator.next();
    }
}
