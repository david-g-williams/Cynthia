package io.cynthia.core.model;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.annotation.PostConstruct;

import io.cynthia.utils.Resources;
import io.cynthia.utils.Serialization;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import org.tensorflow.framework.ConfigProto;
import org.tensorflow.framework.GPUOptions;
import org.tensorflow.SavedModelBundle;

import static io.cynthia.Constants.*;
import static io.cynthia.utils.Resources.readResource;
import static io.cynthia.utils.Serialization.yamlToObject;

@Accessors(fluent = true)
@Component
@Data
@NoArgsConstructor
@Slf4j
public class Loader {
    private final List<SavedModelBundle> savedModelBundles = new ArrayList<>();
    private final Map<String, Model> models = new HashMap<>();

    @PostConstruct
    private void postConstruct() {
        try {
            final Map<String, ModelDef> modelDefs = yamlToObject(readResource(MODELS_YAML), new TypeReference<>() {});
            for(final String modelId : modelDefs.keySet()) {
                models.put(modelId, loadModel(modelId, modelDefs.get(modelId)));
            }
            addShutdownHook();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info(SHUTDOWN_MESSAGE);
            for (SavedModelBundle savedModelBundle : savedModelBundles) {
                savedModelBundle.close();
            }
        }));
    }

    public Model loadModel(final String modelId, final ModelDef modelDef) {
        try {
            final Path archivePath = Paths.get(modelDef.location());
            final Path tempDirectory = Paths.get(System.getProperty(TMP_DIR), CYNTHIA, MODELS);
            final Path unpackDirectory = Paths.get(tempDirectory.toString(), UUID.randomUUID().toString());

            Resources.decompressTarGZArchive(archivePath, unpackDirectory);

            final Model model = new Model().id(modelId);
            final Properties properties = new Properties();
            final Path propertyFile = Paths.get(unpackDirectory.toString(), MODEL_PROPERTIES);

            try (final InputStream propertiesInputStream = new FileInputStream(propertyFile.toFile())) {
                properties.load(propertiesInputStream);
            }

            model.properties(properties);

            final String indexFileName = properties.getProperty(MODEL_INDEX, INDEX_JSON);
            final Path indexFilePath = Paths.get(unpackDirectory.toString(), indexFileName);

            if (Files.exists(indexFilePath)) {
                final byte[] jsonBytes = Resources.readBinaryFile(indexFilePath);
                final String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);
                final Map<String, Object> index = Serialization.jsonToObject(jsonString, new TypeReference<>() {
                });
                model.index(index);
            }

            final String processorName = model.properties().getProperty(MODEL_LAMBDA);

            final Lambda lambda = (Lambda) Class.forName(processorName).getDeclaredConstructor().newInstance();

            model.lambda(lambda);

            final String modelBundle = properties.getProperty(MODEL_BUNDLE, MODEL);

            final String modelPath = Paths.get(unpackDirectory.toString(), modelBundle).toString();

            final ConfigProto configProto = ConfigProto.newBuilder()
                .setAllowSoftPlacement(true)
                .setGpuOptions(GPUOptions
                    .newBuilder()
                    .setPerProcessGpuMemoryFraction(1.00)
                    .build())
                .build();

            final SavedModelBundle savedModelBundle = SavedModelBundle.loader(modelPath)
                .withTags(SERVE)
                .withConfigProto(configProto.toByteArray())
                .load();

            model.session(savedModelBundle.session());

            savedModelBundles.add(savedModelBundle);

            return model;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
