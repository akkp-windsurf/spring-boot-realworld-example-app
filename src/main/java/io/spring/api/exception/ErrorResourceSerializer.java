package io.spring.api.exception;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ErrorResourceSerializer extends JsonSerializer<ErrorResource> {
    @Override
    public void serialize(ErrorResource value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        Map<String, List<String>> json = value.getFieldErrors().stream()
            .collect(Collectors.groupingBy(
                FieldErrorResource::getField,
                Collectors.mapping(FieldErrorResource::getMessage, Collectors.toList())));

        gen.writeStartObject();
        gen.writeObjectFieldStart("errors");
        for (Map.Entry<String, List<String>> pair : json.entrySet()) {
            gen.writeArrayFieldStart(pair.getKey());
            for (String content : pair.getValue()) {
                gen.writeString(content);
            }
            gen.writeEndArray();
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }
}
