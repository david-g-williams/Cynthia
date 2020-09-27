package io.cynthia.utils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Resources {

    public static String readResource(final String resourcePath) throws IOException, URISyntaxException {
        return Files.readString(Paths.get(Resources.class.getResource(resourcePath).toURI()));
    }

    public static byte[] readBinaryFile(final Path binaryFilePath) throws IOException {
        return Files.readAllBytes(binaryFilePath);
    }

    public static void decompressTarGZArchive(final Path archivePath, final Path outputPath) {
        try(final InputStream inputStream = new FileInputStream(archivePath.toFile());
            final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            final GzipCompressorInputStream gzipCompressorInputStream = new GzipCompressorInputStream(bufferedInputStream);
            final TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipCompressorInputStream)) {
            TarArchiveEntry entry;
            while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
                if (entry.isDirectory()) continue;
                final String fileName = entry.getName();
                final Path filePath = Paths.get(outputPath.toString(), fileName);
                Files.copy(tarArchiveInputStream, filePath);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
