package io.cynthia.server.handlers;

import io.cynthia.server.request.Processor;
import io.cynthia.server.request.Request;
import io.cynthia.server.request.Response;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Process {
    
    @Inject
    final Processor processor;

    public Mono<ServerResponse> process(final ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Request.class)
            .flatMap(request ->
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.fromCallable(() -> processor.process(request)), Response.class));
    }
}
