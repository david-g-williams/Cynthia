package io.cynthia.core.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.NonNull;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.beans.factory.annotation.Autowired;
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
import static io.cynthia.Constants.TMP_DIR;

/**
 * To learn about TensorFlow, please see:
 *
 * https://www.tensorflow.org/learn
 */
@Accessors(fluent = true)
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModelLoader {
    @Autowired ModelRegistry modelRegistry;
    Map<String, ModelDefinition> modelDefinitions;
    ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    private void loadModels() throws Exception {
        setupObjectMapper();
        loadModelDefinitions();
        registerModels();
    }

    private void registerModels() {
        for (final String modelId : modelDefinitions.keySet()) {
            modelRegistry.register(loadModel(modelDefinitions.get(modelId)));
        }
    }

    @SneakyThrows
    private void loadModelDefinitions() {
        modelDefinitions = objectMapper.readValue(readResource(MODELS_YAML), new TypeReference<>() {
        });
    }

    private void setupObjectMapper() {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    private Model loadModel(@NonNull final ModelDefinition modelDefinition) {
        final Path archivePath = Paths.get(modelDefinition.location());
        final Path tempDirectory = Paths.get(System.getProperty(TMP_DIR), CYNTHIA, MODELS);
        final Path workingDirectory = Paths.get(tempDirectory.toString(), UUID.randomUUID().toString());
        decompressTarGZArchive(archivePath, workingDirectory);
        final Properties properties = loadModelProperties(workingDirectory);
        final Lambda<?> lambda = loadModelLambda(properties);
        final Map<String, Object> index = loadModelIndex(properties, workingDirectory);
        final Session session = loadTensorFlowSession(modelDefinition, workingDirectory, properties);
        final String modelId = modelDefinition.modelId();
        return Model.of(lambda, index, properties, session, modelId);
    }

    @SneakyThrows
    private Map<String, Object> loadModelIndex(@NonNull final Properties properties, @NonNull final Path modelDirectory) {
        final String indexFileName = properties.getProperty(MODEL_INDEX, INDEX_JSON);
        final Path indexFilePath = Paths.get(modelDirectory.toString(), indexFileName);
        final byte[] indexBytes = readBinaryFile(indexFilePath);
        final String indexString = new String(indexBytes, StandardCharsets.UTF_8);
        return objectMapper.readValue(indexString, new TypeReference<>() {
        });
    }

    @SneakyThrows
    private Lambda<?> loadModelLambda(@NonNull final Properties properties) {
        final String lambdaName = properties.getProperty(MODEL_LAMBDA);
        return (Lambda<?>) Class.forName(lambdaName).getDeclaredConstructor().newInstance();
    }

    private Properties loadModelProperties(@NonNull final Path unpackDirectory) {
        final Path propertyFile = Paths.get(unpackDirectory.toString(), MODEL_PROPERTIES);
        return loadPropertyFile(propertyFile);
    }

    private Session loadTensorFlowSession(@NonNull final ModelDefinition modelDefinition,
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
        @Cleanup final SavedModelBundle savedModelBundle = SavedModelBundle.loader(modelPath)
                .withTags(SERVE)
                .withConfigProto(protoBytes)
                .load();
        return savedModelBundle.session();
    }

    @SneakyThrows
    private String readResource(@NonNull final String resourcePath) {
        return Files.readString(Paths.get(ModelLoader.class.getResource(resourcePath).toURI()));
    }

    @SneakyThrows
    public byte[] readBinaryFile(@NonNull final Path binaryFilePath) {
        return Files.readAllBytes(binaryFilePath);
    }

    @SneakyThrows
    public Properties loadPropertyFile(@NonNull final Path propertyFilePath) {
        final Properties properties = new Properties();
        @Cleanup final InputStream propertiesInputStream = new FileInputStream(propertyFilePath.toFile());
        properties.load(propertiesInputStream);
        return properties;
    }

    @SneakyThrows
    public void decompressTarGZArchive(@NonNull final Path archivePath, @NonNull final Path outputPath) {
        @Cleanup final InputStream inputStream = new FileInputStream(archivePath.toFile());
        @Cleanup final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        @Cleanup final GzipCompressorInputStream gzipCompressorInputStream = new GzipCompressorInputStream(bufferedInputStream);
        @Cleanup final TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipCompressorInputStream);
        TarArchiveEntry entry;
        while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
            if (entry.isDirectory()) continue;
            final String fileName = entry.getName();
            final Path filePath = Paths.get(outputPath.toString(), fileName);
            Files.copy(tarArchiveInputStream, filePath);
        }
    }
}
