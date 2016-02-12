package com.diyphotobooth.lordbritishix.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TemplateTest {
    @Test
    public void canRoundTrip() throws IOException {
        Template template = new Template("blah", Paths.get("/"), 10);
        Template deserializedTemplate = Template.fromJson(new ByteArrayInputStream(template.toJson().getBytes(StandardCharsets.UTF_8)));
        assertThat(template, is(deserializedTemplate));
    }
}
