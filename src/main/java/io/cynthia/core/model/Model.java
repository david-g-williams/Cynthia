package io.cynthia.core.model;

import java.util.Map;
import java.util.Properties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.tensorflow.Session;

@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
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
