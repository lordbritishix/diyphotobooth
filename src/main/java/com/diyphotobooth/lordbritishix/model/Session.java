package com.diyphotobooth.lordbritishix.model;

import com.diyphotobooth.lordbritishix.model.converter.SessionDeserializer;
import com.diyphotobooth.lordbritishix.model.converter.SessionSerializer;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

/**
 * Session represents an active photo shoot session where:
 *
 * numOfPhotosToBeTaken is the number of shots to be taken
 * numberOfPhotosAlreadyTaken is the number of current photos taken
 */
@Data
@Builder
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@JsonSerialize(using = SessionSerializer.class)
@JsonDeserialize(using = SessionDeserializer.class)
public class Session {
    public enum State {
        TAKING_PHOTO,
        DONE_TAKING_PHOTO,
        PREPARING_MONTAGE,
        PRINTING,
        DONE,
        ERROR
    }

    private final UUID sessionId;
    private final int numberOfPhotosToBeTaken;
    private final Template template;
    private final Map<Integer, String> imageMap;
    private final LocalDateTime sessionDate;
    private int numberOfPhotosAlreadyTaken;
    private boolean isPrinted = false;
    private State state;

    public Session(int numberOfPhotosToBeTaken, Template template) {
        if (numberOfPhotosToBeTaken < 0) {
            throw new IllegalStateException("numberOfPhotosToBeTaken must be >= to 0");
        }

        this.sessionId = UUID.randomUUID();
        this.numberOfPhotosToBeTaken = numberOfPhotosToBeTaken;
        this.sessionDate = LocalDateTime.now(ZoneOffset.UTC);
        this.template = template;
        this.imageMap = Maps.newHashMap();
        this.state = State.TAKING_PHOTO;
    }

    public int nextPhoto() {
        if (isSessionFinished()) {
            throw new IllegalStateException("Unable to proceed to the next step - session is complete");
        }

        numberOfPhotosAlreadyTaken++;
        return numberOfPhotosAlreadyTaken;
    }

    public boolean isSessionFinished() {
        return (numberOfPhotosAlreadyTaken >= numberOfPhotosToBeTaken) || (state == State.DONE) || (state == State.ERROR);
    }

    public String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public static Session fromJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, Session.class);
    }

    public static Session fromJsonInFile(File jsonInFile) throws IOException {
        try (InputStream is = new FileInputStream(jsonInFile)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(is, baos);
            String json = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            return Session.fromJson(json);
        }
    }

}
