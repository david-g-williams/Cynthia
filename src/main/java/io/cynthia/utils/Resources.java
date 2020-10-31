package io.cynthia.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Properties;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import lombok.NonNull;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import static io.cynthia.Constants.AVAILABLE_PROCESSORS;

@UtilityClass
public class Resources {
    public static final ExecutorService SHARED_THREAD_POOL = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);

    public static String readResource(@NonNull final String resourcePath) throws Exception {
        return Files.readString(Paths.get(Resources.class.getResource(resourcePath).toURI()));
    }

    public static byte[] readBinaryFile(@NonNull final Path binaryFilePath) throws Exception {
        return Files.readAllBytes(binaryFilePath);
    }

    public static Properties loadPropertyFile(@NonNull final Path propertyFilePath) throws Exception {
        final Properties properties = new Properties();
        @Cleanup final InputStream propertiesInputStream = new FileInputStream(propertyFilePath.toFile());
        properties.load(propertiesInputStream);
        return properties;
    }

    public static void decompressTarGZArchive(@NonNull final Path archivePath, @NonNull final Path outputPath) throws Exception {
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
