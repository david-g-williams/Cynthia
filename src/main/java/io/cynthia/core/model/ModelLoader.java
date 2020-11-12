package io.cynthia.core.model;

import com.fasterxml.jackson.core.type.TypeReference;
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
import io.cynthia.utils.JsonUtils;
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
import static io.cynthia.Constants.MODELS_JSON;
import static io.cynthia.Constants.MODEL_BUNDLE;
import static io.cynthia.Constants.MODEL_INDEX;
import static io.cynthia.Constants.MODEL_LAMBDA;
import static io.cynthia.Constants.MODEL_PROPERTIES;
import static io.cynthia.Constants.MODELS;
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
    @Autowired
    ModelRegistry modelRegistry;

    @PostConstruct
    private void loadModels() throws Exception {
        final String modelDefJson = Files.readString(Paths.get(ModelLoader.class.getResource(MODELS_JSON).toURI()));
        final Map<String, ModelDef> modelDefs =  JsonUtils.toObject(modelDefJson, new TypeReference<>() {});
        for (final String modelId : modelDefs.keySet()) {
            modelRegistry.register(loadModel(modelDefs.get(modelId)));
        }
    }

    private Model loadModel(@NonNull final ModelDef modelDef) {
        final Path archivePath = Paths.get(modelDef.location());
        final Path tempDirectory = Paths.get(System.getProperty(TMP_DIR), CYNTHIA, MODELS);
        final Path workingDirectory = Paths.get(tempDirectory.toString(), UUID.randomUUID().toString());
        decompressTarGZArchive(archivePath, workingDirectory);
        final Properties properties = loadModelProperties(workingDirectory);
        final Lambda<?> lambda = loadModelLambda(properties);
        final Map<String, Object> index = loadModelIndex(properties, workingDirectory);
        final Session session = loadTensorFlowSession(modelDef, workingDirectory, properties);
        final String modelId = modelDef.modelId();
        return Model.of(lambda, index, properties, session, modelId);
    }

    @SneakyThrows
    private Map<String, Object> loadModelIndex(@NonNull final Properties properties, @NonNull final Path modelDirectory) {
        final String indexFileName = properties.getProperty(MODEL_INDEX, INDEX_JSON);
        final Path indexFilePath = Paths.get(modelDirectory.toString(), indexFileName);
        final byte[] indexBytes = Files.readAllBytes(indexFilePath);
        final String indexString = new String(indexBytes, StandardCharsets.UTF_8);
        return JsonUtils.toObject(indexString, new TypeReference<>() {});
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

    private Session loadTensorFlowSession(@NonNull final ModelDef modelDef,
                                          @NonNull final Path unpackDirectory,
                                          @NonNull final Properties properties) {
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
        @Cleanup final SavedModelBundle savedModelBundle = SavedModelBundle.loader(modelPath)
                .withTags(SERVE)
                .withConfigProto(protoBytes)
                .load();
        return savedModelBundle.session();
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
