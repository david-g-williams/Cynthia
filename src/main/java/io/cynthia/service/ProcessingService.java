package io.cynthia.service;

import com.fasterxml.jackson.core.type.TypeReference;

import io.cynthia.core.*;
import io.cynthia.util.Resources;
import io.cynthia.util.YAML;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Component
public class ProcessingService {

    @Inject
    private LoadingService loadingService;

    private final Map<String, Model> models = new HashMap<>();
    private final Map<String, Processor> processors = new HashMap<>();

    @PostConstruct
    private void postConstruct() {
        try {
            Path tempDirectory = Paths.get(System.getProperty("java.io.tmpdir"),  "cynthia", "models");

            Map<String, ModelDef> modelDefs = YAML.toObject(Resources.readResource("/models.yaml"), new TypeReference<>() {});

            for(String modelId : modelDefs.keySet()) {
                ModelDef modelDef = modelDefs.get(modelId);
                Path archivePath = Paths.get(modelDef.getLocation());
                Path unpackDirectory = Paths.get(tempDirectory.toString(), UUID.randomUUID().toString());
                Model model = loadingService.loadModel(modelId, archivePath, unpackDirectory);
                String processorName = model.getProperties().getProperty("model.processor");
                Processor processor = (Processor) Class.forName(processorName).getDeclaredConstructor().newInstance();
                models.put(modelId, model);
                processors.put(modelId, processor);
            }
        } catch (Exception e) {
            log.error("Could not load models.", e);
            throw new RuntimeException(e);
        }
    }

    public Response process(Request request) {
        String modelId = request.getModelId();
        return new Response(modelId, processors.get(modelId).process(request, models.get(modelId)));
    }
}
