package io.cynthia.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;

public class YAML {

    private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    static {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public static <T> T toObject(String yaml, Class<T> tClass) throws IOException {
        return objectMapper.readValue(yaml, tClass);
    }

    public static <T> T toObject(String yaml, TypeReference<T> tTypeReference) throws IOException {
        return objectMapper.readValue(yaml, tTypeReference);
    }
}
