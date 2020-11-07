package io.cynthia.server;

import io.cynthia.server.handlers.ProcessingHandler;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static io.cynthia.Constants.PROCESS;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class Router {

    @Bean
    public RouterFunction<ServerResponse> process(@NonNull final ProcessingHandler processingHandler) {
        return route(POST(PROCESS), processingHandler::process);
    }
}
