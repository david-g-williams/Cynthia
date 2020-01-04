package io.cynthia.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Resources {

    public static String readResource(String resourcePath) throws IOException, URISyntaxException {
        return Files.readString(Paths.get(Resources.class.getResource(resourcePath).toURI()));
    }

    public static byte[] readBinaryFile(Path binaryFilePath) throws IOException {
        return Files.readAllBytes(binaryFilePath);
    }
}
