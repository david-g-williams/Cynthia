package io.cynthia.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.List;

public class Serialization {
    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper();

    static {
        for (final ObjectMapper objectMapper : List.of(JSON_OBJECT_MAPPER, YAML_OBJECT_MAPPER)) {
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        }
    }

    public static String objectToJSON(final Object object) throws JsonProcessingException {
        return JSON_OBJECT_MAPPER.writeValueAsString(object);
    }

    public static <T> T jsonToObject(final String json, final Class<T> tClass) throws IOException {
        return JSON_OBJECT_MAPPER.readValue(json, tClass);
    }

    public static <T> T jsonToObject(final String json, final TypeReference<T> tTypeReference) throws IOException {
        return JSON_OBJECT_MAPPER.readValue(json, tTypeReference);
    }

    public static String objectToYAML(final Object object) throws JsonProcessingException {
        return YAML_OBJECT_MAPPER.writeValueAsString(object);
    }

    public static <T> T yamlToObject(final String yaml, final Class<T> tClass) throws IOException {
        return YAML_OBJECT_MAPPER.readValue(yaml, tClass);
    }

    public static <T> T yamlToObject(final String yaml, final TypeReference<T> tTypeReference) throws IOException {
        return YAML_OBJECT_MAPPER.readValue(yaml, tTypeReference);
    }
}
