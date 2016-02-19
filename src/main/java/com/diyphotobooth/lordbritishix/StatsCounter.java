package com.diyphotobooth.lordbritishix;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.inject.Singleton;

@Singleton
public class StatsCounter {
    private final AtomicInteger picturesTaken = new AtomicInteger();
    private AtomicInteger sessionsCompleted = new AtomicInteger();
    private AtomicInteger sessionsStarted = new AtomicInteger();
    private AtomicInteger sessionsToBeRetried = new AtomicInteger();
    private AtomicInteger sessionsThatEncounteredError = new AtomicInteger();

    public int incrementPicturesTaken() {
        return picturesTaken.incrementAndGet();
    }

    public int incrementSessionsStarted() {
        return sessionsStarted.incrementAndGet();
    }

    public int incrementSessionsToBeRetried() {
        return sessionsToBeRetried.incrementAndGet();
    }

    public int incrementSessionsThatEncounteredError() {
        return sessionsThatEncounteredError.incrementAndGet();
    }


    public int incrementSessionsCompleted() {
        return sessionsCompleted.incrementAndGet();
    }

    public void dump() throws IOException {
        String now = LocalDateTime.now(ZoneOffset.UTC).toString();
        Path file = Files.createFile(Paths.get(System.getProperty("user.dir"), "stats_" + now + ".txt"));
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.toString())))) {
            writer.write("Stats as of " + now);
            writer.newLine();
            writer.write("Pictures Taken: " + picturesTaken.get());
            writer.newLine();
            writer.write("Sessions Started: " + sessionsStarted.get());
            writer.newLine();
            writer.write("Sessions Completed: " + sessionsCompleted.get());
            writer.newLine();
            writer.write("Sessions To Be Retried: " + sessionsToBeRetried.get());
            writer.newLine();
            writer.write("Sessions To That Encountered Errors: " + sessionsThatEncounteredError.get());
        }
    }
}
