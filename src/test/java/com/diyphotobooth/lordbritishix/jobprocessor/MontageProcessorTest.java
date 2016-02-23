package com.diyphotobooth.lordbritishix.jobprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import javax.print.PrintException;
import org.apache.commons.io.FileUtils;
import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMServiceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import com.diyphotobooth.lordbritishix.jobprocessor.montage.DefaultMontageMaker;
import com.diyphotobooth.lordbritishix.model.Session;
import com.diyphotobooth.lordbritishix.model.SessionUtils;
import com.google.common.collect.ImmutableMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MontageProcessorTest {
    private Path snapshotFolder;

    private MontageProcessor fixture;

    @Mock
    private DefaultMontageMaker montageMaker;

    @Mock
    private SessionUtils sessionUtils;

    @Mock
    private GMConnection connection;

    @Before
    public void setup() throws IOException, GMServiceException {
        snapshotFolder = Files.createTempDirectory("");
        fixture = new MontageProcessor(sessionUtils, snapshotFolder.toString(), montageMaker);
        when(sessionUtils.getSessionDir(any(Path.class), any(Session.class))).thenReturn(Paths.get("/"));
        when(montageMaker.apply(any(Session.class), any(Path.class))).thenReturn(Paths.get("/"));
    }

    @After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(snapshotFolder.toFile());
    }

    @Test
    public void acceptProcessesValidSession() {
        Session session = Session.builder()
                .state(Session.State.DONE_TAKING_PHOTO)
                .imageMap(
                    ImmutableMap.of(1, "/home/jim.quitevis/tmp/session/1.png",
                                    2, "/home/jim.quitevis/tmp/session/2.png",
                                    3, "/home/jim.quitevis/tmp/session/3.png",
                                    4, "/home/jim.quitevis/tmp/session/4.png"))
                .sessionId(UUID.randomUUID())
                .numberOfPhotosToBeTaken(4)
                .numberOfPhotosAlreadyTaken(4)
                .build();

        fixture.accept(session);
        verify(sessionUtils).updateSessionStateAndPersistQuietly(any(Path.class), any(Session.class), eq(Session.State.DONE_COMPOSING_MONTAGE));
    }

    @Test
    public void acceptDoesNotUpdateStateWhenMontageMakerFails() {
        Session session = Session.builder()
                .state(Session.State.DONE_TAKING_PHOTO)
                .imageMap(
                        ImmutableMap.of(1, "/home/jim.quitevis/tmp/session/1.png",
                                2, "/home/jim.quitevis/tmp/session/2.png",
                                3, "/home/jim.quitevis/tmp/session/3.png",
                                4, "/home/jim.quitevis/tmp/session/4.png"))
                .sessionId(UUID.randomUUID())
                .numberOfPhotosToBeTaken(4)
                .numberOfPhotosAlreadyTaken(4)
                .build();

        when(montageMaker.apply(any(Session.class), any(Path.class))).thenReturn(null);

        fixture.accept(session);
        verify(sessionUtils).updateSessionStateAndPersistQuietly(any(Path.class), any(Session.class), eq(Session.State.ERROR));
    }

    @Test
    public void acceptDoesNotProcessInvalidSession1() {
        Session session = Session.builder()
                .state(Session.State.TAKING_PHOTO)
                .imageMap(
                        ImmutableMap.of(1, "/home/jim.quitevis/tmp/session/1.png",
                                         2, "/home/jim.quitevis/tmp/session/2.png"))
                .sessionId(UUID.randomUUID())
                .numberOfPhotosToBeTaken(4)
                .numberOfPhotosAlreadyTaken(2)
                .build();

        fixture.accept(session);
        verify(sessionUtils, never()).updateSessionStateAndPersistQuietly(any(Path.class), any(Session.class), any(Session.State.class));
    }

    @Test
    public void acceptDoesNotProcessInvalidSession2() {
        Session session = Session.builder()
                .state(Session.State.DONE_TAKING_PHOTO)
                .imageMap(
                        ImmutableMap.of(1, "/home/jim.quitevis/tmp/session/1.png",
                                         2, "/home/jim.quitevis/tmp/session/2.png"))
                .sessionId(UUID.randomUUID())
                .numberOfPhotosToBeTaken(2)
                .numberOfPhotosAlreadyTaken(1)
                .build();

        fixture.accept(session);
        verify(sessionUtils, never()).updateSessionStateAndPersistQuietly(any(Path.class), any(Session.class), any(Session.State.class));
    }

    @Test
    public void real() throws IOException, PrintException {
//        Session session = Session.builder()
//                .state(Session.State.DONE_TAKING_PHOTO)
//                .imageMap(
//                        ImmutableMap.of(1, "1.png",
//                                2, "2.png",
//                                3, "3.png",
//                                4, "4.png"))
//                .sessionId(UUID.fromString("71bf59fc-d430-11e5-ab30-625662870761"))
//                .numberOfPhotosToBeTaken(4)
//                .numberOfPhotosAlreadyTaken(4)
//                .sessionDate(LocalDateTime.now())
//                .build();
//
//        fixture = new MontageProcessor(new SessionUtils(), "/home/jim.quitevis/tmp", new DefaultMontageMaker(new SimpleGMService(), "/home/jim.quitevis/src/dashboard/settings", Paths.get("/home/jim.quitevis/tmp/").toString(), new SessionUtils()));
//        fixture.accept(session);
//
//        MontagePrinter printer = new MontagePrinter();
//        printer.print(Paths.get("/home/jim.quitevis/tmp/montage.png"));
    }
}
