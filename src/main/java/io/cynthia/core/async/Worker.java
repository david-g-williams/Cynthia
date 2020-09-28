package io.cynthia.core.async;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Set;
import java.util.function.Supplier;

@Accessors(fluent = true)
@Builder
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Worker<T> {
    @Getter boolean completed;
    @Getter boolean exception;
    @Getter boolean interrupted;
    @Getter @Setter boolean parallel;
    @Getter boolean started;
    @Getter double duration;
    Set<Worker<?>> after;
    @Getter String name;
    Supplier<T> lambda;
    @Getter T result;
    @Getter Throwable throwable;

    public boolean isDone() {
        return completed || exception || interrupted;
    }

    public boolean isReady() {
        for (final Worker<?> worker : this.after) {
            if (!worker.isDone()) return false;
        }
        return true;
    }

    public void process() {
        try {
            started = true;
            final long start = System.nanoTime();
            result = lambda.get();
            duration = (System.nanoTime() - start) / 1e6;
            completed = true;
        } catch (Throwable t) {
            exception = true;
            throwable = t;
            throw new RuntimeException(t);
        }
    }
}
