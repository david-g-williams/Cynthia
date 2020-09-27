package io.cynthia.core.model;

import java.util.Map;
import java.util.Properties;

import lombok.*;
import lombok.experimental.Accessors;

import org.tensorflow.Session;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class Model {
    private Lambda lambda;
    private Map<String, Object> index;
    private Properties properties;
    private Session session;
    private String id;
}
