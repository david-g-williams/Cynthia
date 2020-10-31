package io.cynthia.core.exceptions;

import lombok.NonNull;

public class TaskProcessingException extends RuntimeException {
    private TaskProcessingException(@NonNull final Throwable t) {
        super(t);
    }

    public static TaskProcessingException of(@NonNull final Throwable t) {
        return new TaskProcessingException(t);
    }
}
