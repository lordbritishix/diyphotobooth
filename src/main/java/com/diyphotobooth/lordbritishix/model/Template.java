package com.diyphotobooth.lordbritishix.model;

import com.diyphotobooth.lordbritishix.model.serializer.TemplateSerializer;
import lombok.Builder;
import lombok.Data;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@Builder
@JsonSerialize(using = TemplateSerializer.class)
public class Template {
    private String name;
    private Path templateFile;
    private int photoCount;

    public static Template fromJson(Path jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonFile.toFile());

        return Template.builder()
                .name(node.findPath("name").asText())
                .templateFile(Paths.get(node.findPath("templateFile").asText()))
                .photoCount(node.findPath("photoCount").asInt())
                .build();
    }
}
