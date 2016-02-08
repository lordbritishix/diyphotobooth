package com.diyphotobooth.lordbritishix.model;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SessionTest {
    @Test
    public void nextPhotoMovesSessionToNextPhoto() throws IOException {
        Session session = new Session(2, new Template("Default template", Paths.get("/"), 10));

        String ser = session.serialize();

        assertThat(session.nextPhoto(), is(1));
        assertThat(session.isSessionFinished(), is(false));
        assertThat(session.nextPhoto(), is(2));
        assertThat(session.isSessionFinished(), is(true));
    }

    @Test(expected = IllegalStateException.class)
    public void nextPhotoAfterLastThrowsException() throws IOException {
        Session session = new Session(1, null);
        session.nextPhoto();
        session.nextPhoto();
    }

}
