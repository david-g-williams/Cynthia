package io.cynthia.core.async;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.NonNull;
import static io.cynthia.Constants.AVAILABLE_PROCESSORS;

@Accessors(fluent = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Workflow {
    public static final ExecutorService SHARED_THREAD_POOL = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
    String id;
    TaskPool root;

    private Workflow(@NonNull final String id, @NonNull final TaskPool root) {
        this.id = id;
        this.root = root;
    }

    public void process() {
        TaskPool cursor = root;
        while (Objects.nonNull(cursor)) {
            cursor.process();
            final Supplier<TaskPool> next = cursor.next();
            if (Objects.nonNull(next)) {
                cursor = next.get();
            } else {
                return;
            }
        }
    }

    public static Workflow of(@NonNull final TaskPool root) {
        final String id = UUID.randomUUID().toString();
        return Workflow.of(id, root);
    }

    public static Workflow of(@NonNull final String id, @NonNull final TaskPool root) {
        return new Workflow(id, root);
    }
}
