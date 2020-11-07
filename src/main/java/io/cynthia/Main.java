package io.cynthia;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import static io.cynthia.Constants.COMPONENT_SCAN;

/**
 * Cynthia, the Synthetic Intelligent Agent™, Version 1.0.
 */
@ComponentScan(COMPONENT_SCAN)
@SpringBootApplication
public class Main {
    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
