package io.cynthia.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.List;

import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

@UtilityClass
@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
public class Serialization {
    static ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
    static ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper();

    static {
        for (final ObjectMapper objectMapper : List.of(JSON_OBJECT_MAPPER, YAML_OBJECT_MAPPER)) {
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        }
    }

    @SneakyThrows
    public static String objectToJSON(final Object object) {
        return JSON_OBJECT_MAPPER.writeValueAsString(object);
    }

    @SneakyThrows
    public static <T> T jsonToObject(final String json, final Class<T> tClass) {
        return JSON_OBJECT_MAPPER.readValue(json, tClass);
    }

    @SneakyThrows
    public static <T> T jsonToObject(final String json, final TypeReference<T> tTypeReference) {
        return JSON_OBJECT_MAPPER.readValue(json, tTypeReference);
    }

    @SneakyThrows
    public static String objectToYAML(final Object object) {
        return YAML_OBJECT_MAPPER.writeValueAsString(object);
    }

    @SneakyThrows
    public static <T> T yamlToObject(final String yaml, final Class<T> tClass) {
        return YAML_OBJECT_MAPPER.readValue(yaml, tClass);
    }

    @SneakyThrows
    public static <T> T yamlToObject(final String yaml, final TypeReference<T> tTypeReference) {
        return YAML_OBJECT_MAPPER.readValue(yaml, tTypeReference);
    }
}
