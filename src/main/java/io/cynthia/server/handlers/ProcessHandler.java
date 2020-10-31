package io.cynthia.server.handlers;

import io.cynthia.server.request.ModelRequest;
import io.cynthia.server.request.ModelRequestProcessor;
import io.cynthia.server.response.ModelResponse;
import lombok.experimental.Accessors;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Accessors(fluent = true)
@Component
@Getter
public class ProcessHandler {
    @Autowired
    ModelRequestProcessor modelRequestProcessor;

    public Mono<ServerResponse> process(@NonNull final ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ModelRequest.class)
            .flatMap(request ->
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.fromCallable(() -> modelRequestProcessor.process(request)), ModelResponse.class));
    }
}
