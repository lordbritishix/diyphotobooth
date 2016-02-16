package com.diyphotobooth.lordbritishix.model;

import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SessionTest {
    @Test
    public void nextPhotoMovesSessionToNextPhoto() throws IOException {
        Session session = new Session(2);
        assertThat(session.nextPhoto(), is(1));
        assertThat(session.isSessionFinished(), is(false));
        assertThat(session.nextPhoto(), is(2));
        assertThat(session.isSessionFinished(), is(true));
    }

    @Test(expected = IllegalStateException.class)
    public void nextPhotoAfterLastThrowsException() throws IOException {
        Session session = new Session(1);
        session.nextPhoto();
        session.nextPhoto();
    }

    @Test
    public void canRoundtrip() throws IOException {
        Session session = new Session(2);
        session.setPrinted(true);
        session.setMontage(Paths.get("/temp"));
        session.setState(Session.State.PREPARING_MONTAGE);

        session.getImageMap().put(1, "1.jpg");
        session.nextPhoto();

        String json = session.toJson();
        Session deserializedJson = Session.fromJson(json);

        assertThat(session.toString(), is(deserializedJson.toString()));

    }
}
