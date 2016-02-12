package com.diyphotobooth.lordbritishix.jobprocessor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Queue;
import com.diyphotobooth.lordbritishix.model.Session;
import com.google.common.collect.Queues;
import com.google.inject.name.Named;

/**
 * Job Processor is responsible for:
 *
 * 1. Reading the session metadata.
 * 2. Creating a montage from a set of images based on the session metadata
 * 3. Sending the composed montage to the printer for printing
 *
 * Jobs are acquired via:
 * 1. Scanning the snapshotFolder for unprinted sessions
 * 2. Real-time, using queueJob
 */
public class JobProcessor {
    private final Path snapshotFolder;
    private final Queue<Session> jobs = Queues.newLinkedBlockingDeque();

    public JobProcessor(@Named("snapshot.folder") String snapshotFolder) {
        this.snapshotFolder = Paths.get(snapshotFolder);
    }
}
