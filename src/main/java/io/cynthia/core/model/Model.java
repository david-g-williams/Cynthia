package io.cynthia.core.model;

import java.util.Map;
import java.util.Properties;

import lombok.*;
import lombok.experimental.Accessors;

import lombok.experimental.FieldDefaults;
import org.tensorflow.Session;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class Model {
    Lambda lambda;
    Map<String, Object> index;
    Properties properties;
    Session session;
    String id;
}
