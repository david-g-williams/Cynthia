package io.cynthia.server;

import io.cynthia.handler.PredictionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class Router {

    @Bean
    public RouterFunction<ServerResponse> predict(PredictionHandler predictionHandler) {
        return RouterFunctions.route(RequestPredicates.POST("/predict"), predictionHandler::predict);
    }
}
