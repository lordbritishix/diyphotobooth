package com.diyphotobooth.lordbritishix.jobprocessor;

import java.io.IOException;
import java.nio.file.Path;
import javax.print.PrintException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import com.diyphotobooth.lordbritishix.jobprocessor.printer.MontagePrinter;
import com.diyphotobooth.lordbritishix.model.Session;
import com.diyphotobooth.lordbritishix.model.SessionUtils;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PrintProcessorTest {
    private PrintProcessor fixture;

    @Mock
    private MontagePrinter printer;

    @Mock
    private SessionUtils sessionUtils;

    @Before
    public void setup() {
        fixture = new PrintProcessor(sessionUtils, "/", printer);
    }

    @Test
    public void shouldNotPrintIfStateIsInvalid() throws IOException, PrintException {
        fixture.accept(Session.builder().state(Session.State.TAKING_PHOTO).build());
        verify(printer, never()).print(any(Path.class));
    }

    @Test
    public void shouldPrintIfPrinterIsAvailable() throws IOException, PrintException {
        fixture.accept(Session.builder().state(Session.State.DONE_COMPOSING_MONTAGE).build());
        verify(printer).print(any(Path.class));
        verify(sessionUtils).updateSessionStateAndPersistQuietly(any(Path.class), any(Session.class), eq(Session.State.DONE_PRINTING));
    }

    @Test
    public void shouldNotPrintIfPrinterIsNotAvailable() throws IOException, PrintException {
        doThrow(new PrintException()).when(printer).print(any(Path.class));
        fixture.accept(Session.builder().state(Session.State.DONE_COMPOSING_MONTAGE).build());
        verify(printer).print(any(Path.class));
        verify(sessionUtils).updateSessionStateAndPersistQuietly(any(Path.class), any(Session.class), eq(Session.State.RETRY));
    }

    @Test
    public void shouldNotPrintIfImageCannotBeOpened() throws IOException, PrintException {
        doThrow(new IOException()).when(printer).print(any(Path.class));
        fixture.accept(Session.builder().state(Session.State.DONE_COMPOSING_MONTAGE).build());
        verify(printer).print(any(Path.class));
        verify(sessionUtils).updateSessionStateAndPersistQuietly(any(Path.class), any(Session.class), eq(Session.State.RETRY));
    }

    @Test
    public void shouldNotRetryIfPrintEncountersOtherErrors() throws IOException, PrintException {
        doThrow(new NullPointerException()).when(printer).print(any(Path.class));
        fixture.accept(Session.builder().state(Session.State.DONE_COMPOSING_MONTAGE).build());
        verify(printer).print(any(Path.class));
        verify(sessionUtils).updateSessionStateAndPersistQuietly(any(Path.class), any(Session.class), eq(Session.State.ERROR));
    }

}
