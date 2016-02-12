package com.diyphotobooth.lordbritishix.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import com.diyphotobooth.lordbritishix.model.converter.TemplateDeserializer;
import com.diyphotobooth.lordbritishix.model.converter.TemplateSerializer;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
@JsonSerialize(using = TemplateSerializer.class)
@JsonDeserialize(using = TemplateDeserializer.class)
public class Template {
    private String name;
    private Path templateFile;
    private int photoCount;

    public static Template fromJson(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, Template.class);
    }

    public String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
