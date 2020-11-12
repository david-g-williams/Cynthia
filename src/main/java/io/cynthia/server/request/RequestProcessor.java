package io.cynthia.server.request;

import io.cynthia.core.model.ModelRegistry;
import lombok.AccessLevel;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Accessors(fluent = true)
@Controller
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestProcessor {
    @Autowired
    ModelRegistry modelRegistry;

    public Flux<?> process(@NonNull final ModelRequest modelRequest) {
        return modelRegistry.lookup(modelRequest.modelId()).lambda().process(modelRequest);
    }
}
