package com.lda.streambox.json;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class JsonConverter {

    private final ObjectMapper objectMapper;

    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new JsonConverterToJsonException("Failed to serialize object to JSON", e);
        }
    }

    public <T> T fromJson(String value, Class<T> type) {
        try {
            return objectMapper.readValue(value, type);
        } catch (Exception e) {
            throw new JsonConverterFromJsonException("Failed to deserialize JSON to " + type.getSimpleName(), e);
        }
    }
}
