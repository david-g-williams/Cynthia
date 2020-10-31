package io.cynthia.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.List;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.SneakyThrows;

@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Serialization {
    ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
    ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper();

    static {
        for (final ObjectMapper objectMapper : List.of(JSON_OBJECT_MAPPER, YAML_OBJECT_MAPPER)) {
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        }
    }

    @SneakyThrows
    public static String objectToJSON(@NonNull final Object object) {
        return JSON_OBJECT_MAPPER.writeValueAsString(object);
    }

    @SneakyThrows
    public static <T> T jsonToObject(@NonNull final String json, @NonNull final Class<T> tClass) {
        return JSON_OBJECT_MAPPER.readValue(json, tClass);
    }

    @SneakyThrows
    public static <T> T jsonToObject(@NonNull final String json, @NonNull final TypeReference<T> tTypeReference) {
        return JSON_OBJECT_MAPPER.readValue(json, tTypeReference);
    }

    @SneakyThrows
    public static String objectToYAML(@NonNull final Object object) {
        return YAML_OBJECT_MAPPER.writeValueAsString(object);
    }

    @SneakyThrows
    public static <T> T yamlToObject(@NonNull final String yaml, @NonNull final Class<T> tClass) {
        return YAML_OBJECT_MAPPER.readValue(yaml, tClass);
    }

    @SneakyThrows
    public static <T> T yamlToObject(@NonNull final String yaml, @NonNull final TypeReference<T> tTypeReference) {
        return YAML_OBJECT_MAPPER.readValue(yaml, tTypeReference);
    }
}
