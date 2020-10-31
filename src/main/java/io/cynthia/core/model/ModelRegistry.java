package io.cynthia.core.model;

import io.cynthia.core.exceptions.ModelNotFoundException;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import java.util.HashMap;
import java.util.Map;

@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModelRegistry {
    Map<String, Model> models = new HashMap<>();

    public Model lookup(@NonNull final String modelId) {
        if (!models.containsKey(modelId)) {
            throw ModelNotFoundException.of(modelId);
        }
        return models.get(modelId);
    }

    public void register(@NonNull final Model model) {
        models.put(model.id(), model);
    }
}
