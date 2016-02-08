package com.diyphotobooth.lordbritishix.model;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class SessionManagerTest {
    private SessionManager fixture;

    @Before
    public void setup() throws IOException {
        fixture = new SessionManager(Files.createTempDirectory("").toString());
    }

    @Test
    public void getReturnsUnfinishedSession() throws IOException {
        Session session = fixture.getOrCreateNewSession(5, false, null);
        assertThat(session, is(fixture.getOrCreateNewSession(5, false, null)));
    }

    @Test
    public void getReturnsNewSessionIfForced() throws IOException {
        Session session = fixture.getOrCreateNewSession(5, true, null);
        assertThat(session, not(fixture.getOrCreateNewSession(5, true, null)));
    }

    @Test
    public void getReturnsNewSessionExistingSessionIsDone() throws IOException {
        Session session = fixture.getOrCreateNewSession(1, false, null);
        session.nextPhoto();
        assertThat(session, not(fixture.getOrCreateNewSession(1, false, null)));
    }

}
