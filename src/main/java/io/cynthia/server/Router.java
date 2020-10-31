package io.cynthia.server;

import io.cynthia.Constants;
import io.cynthia.server.handlers.ProcessHandler;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class Router {

    @Bean
    public RouterFunction<ServerResponse> process(@NonNull final ProcessHandler processHandler) {
        return RouterFunctions.route(RequestPredicates.POST(Constants.PROCESS), processHandler::process);
    }
}
