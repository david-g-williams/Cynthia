package io.cynthia.core.async;

import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.NonNull;

@Accessors(fluent = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Workflow {
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
            if (Objects.nonNull(cursor.next())) {
                cursor = cursor.next().get();
            } else {
                return;
            }
        }
    }

    public static Workflow of(@NonNull final TaskPool root) {
        return Workflow.of(UUID.randomUUID().toString(), root);
    }

    public static Workflow of(@NonNull final String id, @NonNull final TaskPool root) {
        return new Workflow(id, root);
    }
}
