package io.cynthia.server.response;

import lombok.NonNull;
import lombok.Value;
import java.util.List;
import java.util.Map;

@Value
public class ModelResponse {
    List<Map<String, Object>> data;

    private ModelResponse(@NonNull final List<Map<String, Object>> data) {
        this.data = data;
    }

    public static ModelResponse of(@NonNull final List<Map<String, Object>> data) {
        return new ModelResponse(data);
    }
}
