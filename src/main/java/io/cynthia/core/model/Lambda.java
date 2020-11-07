package io.cynthia.core.model;

import io.cynthia.server.request.ModelRequest;
import lombok.NonNull;
import reactor.core.publisher.Flux;

public abstract class Lambda<T> {
    public abstract Flux<T> process(@NonNull final ModelRequest modelRequest);
}
