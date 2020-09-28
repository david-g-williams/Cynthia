package io.cynthia.core.async;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.function.Supplier;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class Worker<T> {
    private boolean completed;
    private boolean exception;
    private boolean interrupted;
    private boolean parallel;
    private boolean started;
    private double duration;
    private Set<Worker<?>> after;
    private String name;
    private Supplier<T> lambda;
    private T result;
    private Throwable throwable;

    public boolean isDone() {
        return completed || exception || interrupted;
    }

    public boolean isReady() {
        for (final Worker<?> worker : this.after)
            if (!worker.isDone()) return false;
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
