package io.cynthia.core;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface Processor {
    List<Map<String, Object>> process(Request request, Model model);
}
