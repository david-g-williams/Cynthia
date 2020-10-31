package io.cynthia.server.request;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
@Value
public class ModelRequest {
    List<Map<String, Object>> data;
    String modelId;

    private ModelRequest(@NonNull final List<Map<String, Object>> data, @NonNull final String modelId) {
        this.data = data;
        this.modelId = modelId;
    }

    public static ModelRequest of(@NonNull final List<Map<String, Object>> data, @NonNull final String modelId) {
        return new ModelRequest(data, modelId);
    }
}
