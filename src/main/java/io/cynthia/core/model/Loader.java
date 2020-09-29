package io.cynthia.core.model;

import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.annotation.PostConstruct;

import io.cynthia.utils.Resources;
import io.cynthia.utils.Serialization;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.tensorflow.Session;
import org.tensorflow.framework.ConfigProto;
import org.tensorflow.framework.GPUOptions;
import org.tensorflow.SavedModelBundle;

import static io.cynthia.Constants.*;
import static io.cynthia.utils.Resources.loadPropertyFile;
import static io.cynthia.utils.Resources.readResource;
import static io.cynthia.utils.Serialization.yamlToObject;

@Accessors(fluent = true)
@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class Loader {
    List<SavedModelBundle> savedModelBundles = new ArrayList<>();
    @Getter Map<String, Model> models = new HashMap<>();

    @PostConstruct
    private void loadModels() throws Exception {
        final Map<String, ModelDef> modelDefs = yamlToObject(readResource(MODELS_YAML), new TypeReference<>() {});
        for(final String modelId : modelDefs.keySet()) {
            models.put(modelId, loadModel(modelId, modelDefs.get(modelId)));
        }
        addShutdownHook();
    }

    private Model loadModel(final String modelId, final ModelDef modelDef) throws Exception {
        final Path archivePath = Paths.get(modelDef.location());
        final Path tempDirectory = Paths.get(System.getProperty(TMP_DIR), CYNTHIA, MODELS);
        final Path unpackDirectory = Paths.get(tempDirectory.toString(), UUID.randomUUID().toString());
        Resources.decompressTarGZArchive(archivePath, unpackDirectory);
        final Properties properties = loadModelProperties(unpackDirectory);
        return Model.builder()
            .id(modelId)
            .index(loadModelIndex(properties, unpackDirectory))
            .lambda(loadModelLambda(properties))
            .properties(properties)
            .session(loadSession(modelDef, properties, unpackDirectory))
            .build();
    }

    private Properties loadModelProperties(final Path unpackDirectory) throws Exception {
        final Path propertyFile = Paths.get(unpackDirectory.toString(), MODEL_PROPERTIES);
        return loadPropertyFile(propertyFile);
    }

    private Map<String, Object> loadModelIndex(final Properties properties, final Path unpackDirectory) throws Exception {
        final String indexFileName = properties.getProperty(MODEL_INDEX, INDEX_JSON);
        final Path indexFilePath = Paths.get(unpackDirectory.toString(), indexFileName);
        final byte[] jsonBytes = Resources.readBinaryFile(indexFilePath);
        final String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);
        return Serialization.jsonToObject(jsonString, new TypeReference<>() {});
    }

    private Lambda loadModelLambda(final Properties properties) throws Exception {
        final String lambdaName = properties.getProperty(MODEL_LAMBDA);
        return (Lambda) Class.forName(lambdaName).getDeclaredConstructor().newInstance();
    }

    private Session loadSession(final ModelDef modelDef, final Properties properties, final Path unpackDirectory) {
        final String modelBundle = properties.getProperty(MODEL_BUNDLE, MODEL);
        final String modelPath = Paths.get(unpackDirectory.toString(), modelBundle).toString();
        final double gpuFraction = modelDef.gpuFraction();
        final GPUOptions gpuOptions = GPUOptions.newBuilder()
            .setPerProcessGpuMemoryFraction(gpuFraction)
            .build();
        final ConfigProto configProto = ConfigProto.newBuilder()
            .setAllowSoftPlacement(true)
            .setGpuOptions(gpuOptions)
            .build();
        final byte[] protoBytes = configProto.toByteArray();
        final SavedModelBundle savedModelBundle = SavedModelBundle.loader(modelPath)
            .withTags(SERVE)
            .withConfigProto(protoBytes)
            .load();
        savedModelBundles.add(savedModelBundle);
        return savedModelBundle.session();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info(SHUTDOWN_MESSAGE);
            for (final SavedModelBundle savedModelBundle : savedModelBundles) {
                savedModelBundle.close();
            }
        }));
    }
}
