package io.cynthia.handlers;

import io.cynthia.core.Greeting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class GreetingHandler {


    public Mono<ServerResponse> hello(ServerRequest serverRequest) {
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(new Greeting("Hello!")), Greeting.class);
    }

    public Mono<ServerResponse> goodbye(ServerRequest serverRequest) {
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(new Greeting("Goodbye!")), Greeting.class);
    }
}
