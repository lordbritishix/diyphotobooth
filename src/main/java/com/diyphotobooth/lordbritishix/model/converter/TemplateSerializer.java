package com.diyphotobooth.lordbritishix.model.converter;

import com.diyphotobooth.lordbritishix.model.Template;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

public class TemplateSerializer extends JsonSerializer<Template> {
    @Override
    public void serialize(Template template, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("name", template.getName());
        jsonGenerator.writeStringField("templateFile", template.getTemplateFile().toString());
        jsonGenerator.writeNumberField("photoCount", template.getPhotoCount());
        jsonGenerator.writeEndObject();

    }
}
