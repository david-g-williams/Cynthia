package io.cynthia.server.request;

import io.cynthia.core.model.Loader;
import io.cynthia.core.model.Model;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class Processor {

    @Inject
    private final Loader loader;

    public Response process(final Request request) {
        final String modelId = request.modelId();
        final Model model = loader.models().get(modelId);
        final List<?> results = model.lambda().process(request, model);
        return Response.builder()
            .modelId(modelId)
            .results(results)
            .build();
    }
}
