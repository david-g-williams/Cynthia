package io.cynthia.handler;

import io.cynthia.core.Request;
import io.cynthia.core.Response;
import io.cynthia.service.PredictionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@Slf4j
@Component
public class PredictionHandler {

    @Inject
    PredictionService predictionService;

    public Mono<ServerResponse> predict(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Request.class)
            .flatMap(request ->
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.fromCallable(() ->
                        predictionService.predict(request)), Response.class));
    }
}
