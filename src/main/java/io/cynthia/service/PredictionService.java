package io.cynthia.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.cynthia.core.Model;
import io.cynthia.core.Request;
import io.cynthia.core.Response;
import io.cynthia.util.Resources;
import io.cynthia.util.YAML;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PredictionService {

    @Inject
    private Resources resources;

    private Map<String, Model> models;

    @PostConstruct
    private void postConstruct() {
        try {
            models = YAML.toObject(resources.readResource("/models.yaml"), new TypeReference<>() {});
        } catch (IOException | URISyntaxException e) {
            log.error("Could not load models.", e);
            throw new RuntimeException(e);
        }
    }

    public Response predict(Request request) {
        Model model = models.get(request.modelId());
        return null;
    }
}
