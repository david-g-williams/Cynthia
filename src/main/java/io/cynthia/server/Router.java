package io.cynthia.server;

import io.cynthia.Constants;
import io.cynthia.server.handlers.Processing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class Router {

    @Bean
    public RouterFunction<ServerResponse> process(final Processing processing) {
        return RouterFunctions.route(RequestPredicates.POST(Constants.PROCESS), processing::process);
    }
}
