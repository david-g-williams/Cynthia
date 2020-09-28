package io.cynthia.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import lombok.Cleanup;
import lombok.experimental.UtilityClass;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

@UtilityClass
public class Resources {

    public static String readResource(final String resourcePath) throws Exception {
        return Files.readString(Paths.get(Resources.class.getResource(resourcePath).toURI()));
    }

    public static byte[] readBinaryFile(final Path binaryFilePath) throws Exception {
        return Files.readAllBytes(binaryFilePath);
    }

    public static Properties loadPropertyFile(final Path propertyFilePath) throws Exception {
        final Properties properties = new Properties();
        @Cleanup final InputStream propertiesInputStream = new FileInputStream(propertyFilePath.toFile());
        properties.load(propertiesInputStream);
        return properties;
    }

    public static void decompressTarGZArchive(final Path archivePath, final Path outputPath) throws Exception {
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
