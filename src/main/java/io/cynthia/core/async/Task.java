package io.cynthia.core.async;

import io.cynthia.core.exceptions.TaskProcessingException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@Accessors(fluent = true)
@Getter
public class Task<T> {
    boolean interrupted;
    Instant created;
    Instant finished;
    Instant started;
    @NonNull Set<Task<?>> after;
    @NonNull String id;
    @NonNull Supplier<T> supplier;
    @Setter boolean parallel;
    @Setter Instant completed;
    @Setter Instant submitted;
    T result;

    private Task(@NonNull final Set<Task<?>> after,
                 @NonNull final String id,
                 @NonNull final Supplier<T> supplier) {
        this.after = after;
        this.created = Instant.now();
        this.id = id;
        this.supplier = supplier;
    }

    public static <T> Task<T> of(@NonNull final String id, @NonNull final Supplier<T> supplier) {
        return Task.of(Set.of(), id, supplier);
    }

    public static <T> Task<T> of(@NonNull final Supplier<T> supplier) {
        return Task.of(Set.of(), UUID.randomUUID().toString(), supplier);
    }

    public static <T> Task<T> of(@NonNull final Set<Task<?>> after,
                                 @NonNull final String id,
                                 @NonNull final Supplier<T> supplier) {
        return new Task<T>(after, id, supplier);
    }

    public boolean isCompleted() {
        return Objects.nonNull(result()) || interrupted();
    }

    public boolean isReady() {
        for (final Task<?> task : after()) {
            if (!task.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    public void process() {
        try {
            started = Instant.now();
            result = supplier.get();
            finished = Instant.now();
        } catch (final Throwable t) {
            throw TaskProcessingException.of(t);
        }
    }
}
