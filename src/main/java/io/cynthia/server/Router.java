package io.cynthia.server;

import io.cynthia.handler.ProcessingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class Router {

    @Bean
    public RouterFunction<ServerResponse> predict(ProcessingHandler processingHandler) {
        return RouterFunctions.route(RequestPredicates.POST("/process"), processingHandler::process);
    }
}
