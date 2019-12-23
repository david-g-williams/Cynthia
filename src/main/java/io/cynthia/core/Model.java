package io.cynthia.core;

import lombok.*;
import lombok.experimental.Accessors;
import org.tensorflow.Session;

import java.util.Map;
import java.util.Properties;

@Accessors(fluent = true, chain = true)
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
public class Model {
    private Map<String, Object> index;
    private Properties properties;
    private Session session;
    private String location;
    private String id;
}
