package io.cynthia.core.exceptions;

import lombok.NonNull;
import static io.cynthia.Constants.MODEL_NOT_FOUND;

public class ModelNotFoundException extends RuntimeException {
    private ModelNotFoundException(@NonNull final String message) {
        super(message);
    }

    public static ModelNotFoundException of(@NonNull final String modelId) {
        return new ModelNotFoundException(String.format(MODEL_NOT_FOUND, modelId));
    }
}
