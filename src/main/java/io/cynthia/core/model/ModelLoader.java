package io.cynthia.core.model;

import com.fasterxml.jackson.core.type.TypeReference;
import io.cynthia.utils.Resources;
import io.cynthia.utils.Serialization;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tensorflow.framework.ConfigProto;
import org.tensorflow.framework.GPUOptions;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import static io.cynthia.Constants.CYNTHIA;
import static io.cynthia.Constants.INDEX_JSON;
import static io.cynthia.Constants.MODEL;
import static io.cynthia.Constants.MODEL_BUNDLE;
import static io.cynthia.Constants.MODEL_INDEX;
import static io.cynthia.Constants.MODEL_LAMBDA;
import static io.cynthia.Constants.MODEL_PROPERTIES;
import static io.cynthia.Constants.MODELS;
import static io.cynthia.Constants.MODELS_YAML;
import static io.cynthia.Constants.SERVE;
import static io.cynthia.Constants.SHUTDOWN_MESSAGE;
import static io.cynthia.Constants.TMP_DIR;
import static io.cynthia.utils.Resources.decompressTarGZArchive;
import static io.cynthia.utils.Resources.loadPropertyFile;
import static io.cynthia.utils.Resources.readResource;
import static io.cynthia.utils.Serialization.yamlToObject;

@Accessors(fluent = true)
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ModelLoader {
    List<SavedModelBundle> savedModelBundles = new ArrayList<>();
    ModelRegistry modelRegistry;

    @PostConstruct
    private void loadModels() throws Exception {
        final Map<String, ModelDefinition> modelDefinitions = yamlToObject(readResource(MODELS_YAML), new TypeReference<>() {
        });
        for (final String modelId : modelDefinitions.keySet()) {
            modelRegistry.register(loadModel(modelDefinitions.get(modelId)));
        }
        addShutdownHook();
    }

    private Model loadModel(@NonNull final ModelDefinition modelDefinition) throws Exception {
        final Path archivePath = Paths.get(modelDefinition.location());
        final Path tempDirectory = Paths.get(System.getProperty(TMP_DIR), CYNTHIA, MODELS);
        final Path workingDirectory = Paths.get(tempDirectory.toString(), UUID.randomUUID().toString());
        decompressTarGZArchive(archivePath, workingDirectory);
        final Properties properties = loadModelProperties(workingDirectory);
        final Lambda<?> lambda = loadModelLambda(properties);
        final Map<String, Object> index = loadModelIndex(properties, workingDirectory);
        final Session session = loadSession(modelDefinition, workingDirectory, properties);
        final String modelId = modelDefinition.modelId();
        return Model.of(lambda, index, properties, session, modelId);
    }

    private Properties loadModelProperties(@NonNull final Path unpackDirectory) throws Exception {
        final Path propertyFile = Paths.get(unpackDirectory.toString(), MODEL_PROPERTIES);
        return loadPropertyFile(propertyFile);
    }

    private Map<String, Object> loadModelIndex(@NonNull final Properties properties,
                                               @NonNull final Path unpackDirectory) throws Exception {
        final String indexFileName = properties.getProperty(MODEL_INDEX, INDEX_JSON);
        final Path indexFilePath = Paths.get(unpackDirectory.toString(), indexFileName);
        final byte[] jsonBytes = Resources.readBinaryFile(indexFilePath);
        final String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);
        return Serialization.jsonToObject(jsonString, new TypeReference<>() {
        });
    }

    private Lambda<?> loadModelLambda(@NonNull final Properties properties) throws Exception {
        final String lambdaName = properties.getProperty(MODEL_LAMBDA);
        return (Lambda<?>) Class.forName(lambdaName).getDeclaredConstructor().newInstance();
    }

    private Session loadSession(@NonNull final ModelDefinition modelDefinition,
                                @NonNull final Path unpackDirectory,
                                @NonNull final Properties properties) {
        final String modelBundle = properties.getProperty(MODEL_BUNDLE, MODEL);
        final String modelPath = Paths.get(unpackDirectory.toString(), modelBundle).toString();
        final double gpuFraction = modelDefinition.gpuFraction();
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
