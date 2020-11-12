package io.cynthia.core.model;

import java.util.Map;
import java.util.Properties;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.tensorflow.Session;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Value
public class Model {
    Lambda<?> lambda;
    Map<String, Object> index;
    Properties properties;
    Session session;
    String id;

    public static Model of(@NonNull final Lambda<?> lambda,
                           @NonNull final Map<String, Object> index,
                           @NonNull final Properties properties,
                           @NonNull final Session session,
                           @NonNull final String id) {
        return new Model(lambda, index, properties, session, id);
    }
}
