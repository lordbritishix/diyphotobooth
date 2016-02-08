package com.diyphotobooth.lordbritishix.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

/**
 * Session represents an active photo shoot session where:
 *
 * numOfPhotosToBeTaken is the number of shots to be taken
 * numOfCurrentPhotosTaken is the number of current photos taken
 */
@ToString
public class Session {
    private final UUID sessionId;
    private final int numOfPhotosToBeTaken;
    private int numOfCurrentPhotosTaken;
    private final LocalDateTime dateTime;
    private boolean forceComplete = false;
    private boolean isPrinted = false;
    private final Template template;
    private final Map<Integer, String> imageMap;

    public Session(int numOfPhotosToBeTaken, Template template) {
        if (numOfPhotosToBeTaken < 0) {
            throw new IllegalStateException("numOfPhotosToBeTaken must be greater than or equal to 0");
        }

        this.sessionId = UUID.randomUUID();
        this.numOfPhotosToBeTaken = numOfPhotosToBeTaken;
        this.dateTime = LocalDateTime.now(ZoneOffset.UTC);
        this.template = template;
        this.imageMap = Maps.newConcurrentMap();
    }

    public synchronized boolean isSessionFinished() {
        return (numOfPhotosToBeTaken == numOfCurrentPhotosTaken) || (forceComplete);
    }

    public synchronized int nextPhoto() {
        if (isSessionFinished()) {
            throw new IllegalStateException("Unable to proceed to the next step - session is complete");
        }

        numOfCurrentPhotosTaken++;

        return numOfCurrentPhotosTaken;
    }

    public void forceCompleteSession() {
        forceComplete = true;
    }

    public boolean isSessionCompletedByForce() {
        return forceComplete;
    }

    public void setIsPrinted(boolean isPrinted) {
        this.isPrinted = isPrinted;
    }

    public boolean isPrinted() {
        return this.isPrinted;
    }

    public Template getTemplate() {
        return template;
    }

    public synchronized int getPhotoCountTaken() {
        return numOfCurrentPhotosTaken;
    }

    public void mapImageWithCurrentId(String imageName) {
        imageMap.put(getPhotoCountTaken(), imageName);
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public int getNumOfPhotosToBeTaken() {
        return numOfPhotosToBeTaken;
    }

    @JsonIgnore
    public LocalDateTime getSessionDate() {
        return dateTime;
    }

    public long getSessionDateAsEpoch() {
        return  dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public Map<Integer, String> getImageMap() {
        return ImmutableMap.copyOf(imageMap);
    }

    public String serialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

}
