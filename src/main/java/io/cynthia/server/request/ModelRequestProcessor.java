package io.cynthia.server.request;

import io.cynthia.core.model.ModelRegistry;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.experimental.Accessors;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Accessors(fluent = true)
@Controller
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModelRequestProcessor {
    @Autowired
    ModelRegistry modelRegistry;

    public Stream<?> process(@NonNull final ModelRequest modelRequest) {
        return modelRegistry.lookup(modelRequest.modelId()).lambda().process(modelRequest);
    }
}
