package io.cynthia.server;

import io.cynthia.handlers.GreetingHandler;
import io.cynthia.handlers.PredictionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class Router {

    @Bean
    public RouterFunction<ServerResponse> greeting(GreetingHandler greetingHandler) {
        return RouterFunctions.route(RequestPredicates.GET("/hello"), greetingHandler::hello)
            .andRoute(RequestPredicates.GET("/goodbye"), greetingHandler::goodbye);
    }

    @Bean
    public RouterFunction<ServerResponse> predict(PredictionHandler predictionHandler) {
        return RouterFunctions.route(RequestPredicates.POST("/predict"), predictionHandler::predict);
    }
}
