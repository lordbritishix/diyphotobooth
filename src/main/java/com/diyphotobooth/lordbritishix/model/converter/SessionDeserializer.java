package com.diyphotobooth.lordbritishix.model.converter;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import com.diyphotobooth.lordbritishix.model.Session;

public class SessionDeserializer extends JsonDeserializer<Session> {
    @Override
    public Session deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode tree = jsonParser.readValueAsTree();

        ObjectMapper mapper = new ObjectMapper();
        Session session = Session.builder()
                .sessionDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(tree.get("sessionDateAsEpoch").asLong()), ZoneOffset.UTC))
                .state(Session.State.valueOf(tree.get("state").getTextValue()))
                .isPrinted(tree.get("isPrinted").getBooleanValue())
                .numberOfPhotosAlreadyTaken(tree.get("numberOfPhotosAlreadyTaken").getIntValue())
                .numberOfPhotosToBeTaken(tree.get("numberOfPhotosToBeTaken").getIntValue())
                .sessionId(UUID.fromString(tree.get("sessionId").getTextValue()))
                .imageMap(mapper.readValue(tree.get("imageMap"), Map.class))
                .montage(Paths.get(tree.get("montage").getTextValue()))
                .build();

        return session;
    }
}
