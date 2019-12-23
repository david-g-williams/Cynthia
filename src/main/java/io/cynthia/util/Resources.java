package io.cynthia.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Component
public class Resources {

    public String readResource(String resourcePath) throws IOException, URISyntaxException {
        return Files.readString(Paths.get(getClass().getResource(resourcePath).toURI()));
    }
}
