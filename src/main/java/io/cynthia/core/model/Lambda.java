package io.cynthia.core.model;

import io.cynthia.server.request.ModelRequest;
import java.util.stream.Stream;
import lombok.NonNull;

public abstract class Lambda<T> {
    public abstract Stream<T> process(@NonNull final ModelRequest modelRequest);
}
