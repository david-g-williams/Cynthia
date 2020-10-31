package io.cynthia.core.async;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.cynthia.core.Environment;
import io.cynthia.utils.Resources;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
@RequiredArgsConstructor
public class TaskPool {
    String id;
    Set<Task<?>> tasks;
    Supplier<TaskPool> next;

    public void process() {
        final Deque<Task<?>> deque = new ArrayDeque<>(tasks());
        final List<CompletableFuture<?>> completableFutures = new ArrayList<>();
        while (deque.size() > 0) {
            final Task<?> task = deque.poll();
            if (task.isReady()) {
                if (task.parallel()) {
                    task.submitted(Instant.now());
                    final CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
                        task.process();
                        return null;
                    }, Resources.SHARED_THREAD_POOL);
                    completableFuture.thenAccept(completed -> task.completed(Instant.now()));
                    completableFutures.add(completableFuture);
                } else {
                    task.process();
                }
            } else {
                deque.add(task);
            }
        }

        final CompletableFuture<?>[] futureArray = completableFutures.toArray(new CompletableFuture[0]);

        if (futureArray.length > 0) {
            CompletableFuture.allOf(futureArray).join();
        }
    }
}
