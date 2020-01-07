package io.cynthia.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.cynthia.core.Model;
import io.cynthia.util.JSON;
import io.cynthia.util.Resources;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.stereotype.Component;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.framework.ConfigProto;
import org.tensorflow.framework.GPUOptions;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Component
public class LoadingService {

    private List<SavedModelBundle> savedModelBundles = new ArrayList<>();

    @PostConstruct
    private void postConstruct() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Closing all SavedModelBundle");
            for (SavedModelBundle savedModelBundle : savedModelBundles) {
                savedModelBundle.close();
            }
        }));
    }

    public Model loadModel(String modelId, Path archivePath, Path unpackDirectory) throws IOException {
        Model model = new Model().setId(modelId);

        try(InputStream inputStream = new FileInputStream(archivePath.toFile());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            GzipCompressorInputStream gzipCompressorInputStream = new GzipCompressorInputStream(bufferedInputStream);
            TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipCompressorInputStream)) {
            TarArchiveEntry entry;
            while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
                if (entry.isDirectory()) continue;
                String fileName = entry.getName();
                Path filePath = Paths.get(unpackDirectory.toString(), fileName);
                Files.copy(tarArchiveInputStream, filePath);
            }
        }

        Properties properties = new Properties();
        Path propertyFile = Paths.get(unpackDirectory.toString(), "model.properties");
        try(InputStream propertiesInputStream = new FileInputStream(propertyFile.toFile())) {
            properties.load(propertiesInputStream);
        } catch (Exception e) {
            log.error("Could not load model.properties", e);
            throw new RuntimeException(e);
        }
        model.setProperties(properties);

        String indexFileName = properties.getProperty("model.index", "index.json");
        Map<String, Object> index = null;
        Path indexFile = Paths.get(unpackDirectory.toString(), indexFileName);
        if(Files.exists(indexFile)) {
            byte[] indexJsonBytes = Resources.readBinaryFile(indexFile);
            index = JSON.toObject(new String(indexJsonBytes, StandardCharsets.UTF_8), new TypeReference<>() {});
        }
        model.setIndex(index);

        String modelBundle = properties.getProperty("model.bundle", "model");

        String modelPath = Paths.get(unpackDirectory.toString(), modelBundle).toString();

        ConfigProto configProto = ConfigProto.newBuilder()
            .setAllowSoftPlacement(true)
            .setGpuOptions(GPUOptions
                .newBuilder()
                .setPerProcessGpuMemoryFraction(1.00)
                .build())
            .build();

        SavedModelBundle savedModelBundle = SavedModelBundle.loader(modelPath)
            .withTags("serve")
            .withConfigProto(configProto.toByteArray())
            .load();

        model.setSession(savedModelBundle.session());

        savedModelBundles.add(savedModelBundle);

        return model;
    }
}
