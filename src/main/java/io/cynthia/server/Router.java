package io.cynthia.server;

import io.cynthia.Constants;
import io.cynthia.server.handlers.Process;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class Router {

    @Bean
    public RouterFunction<ServerResponse> process(final Process process) {
        return RouterFunctions.route(RequestPredicates.POST(Constants.PROCESS), process::process);
    }
}
