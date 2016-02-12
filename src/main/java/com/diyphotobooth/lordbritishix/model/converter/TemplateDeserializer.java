package com.diyphotobooth.lordbritishix.model.converter;

import java.io.IOException;
import java.nio.file.Paths;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import com.diyphotobooth.lordbritishix.model.Template;

public class TemplateDeserializer extends JsonDeserializer<Template> {
    @Override
    public Template deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.readValueAsTree();
        Template template = Template.builder()
                .name(node.get("name").getTextValue())
                .photoCount(node.get("photoCount").getIntValue())
                .templateFile(Paths.get(node.get("templateFile").getTextValue()))
                .build();

        return template;
    }
}
