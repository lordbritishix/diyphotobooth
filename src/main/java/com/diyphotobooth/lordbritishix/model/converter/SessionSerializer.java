package com.diyphotobooth.lordbritishix.model.converter;

import java.io.IOException;
import java.time.ZoneOffset;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import com.diyphotobooth.lordbritishix.model.Session;

public class SessionSerializer extends JsonSerializer<Session> {
    @Override
    public void serialize(Session session, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("sessionId", session.getSessionId().toString());
        jsonGenerator.writeNumberField("numberOfPhotosToBeTaken", session.getNumberOfPhotosToBeTaken());
        jsonGenerator.writeNumberField("numberOfPhotosAlreadyTaken", session.getNumberOfPhotosAlreadyTaken());
        jsonGenerator.writeNumberField("sessionDateAsEpoch", session.getSessionDate().toInstant(ZoneOffset.UTC).toEpochMilli());
        jsonGenerator.writeBooleanField("isPrinted", session.isPrinted());
        jsonGenerator.writeStringField("state", session.getState().name());
        jsonGenerator.writeObjectField("imageMap", session.getImageMap());
        jsonGenerator.writeObjectField("montage", session.getMontage() != null ? session.getMontage().toString() : "");
    }
}
