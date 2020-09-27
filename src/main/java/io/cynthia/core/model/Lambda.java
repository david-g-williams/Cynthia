package io.cynthia.core.model;

import io.cynthia.server.request.Request;

import java.util.List;

@FunctionalInterface
public interface Lambda {
    List<?> process(Request request, Model model);
}
