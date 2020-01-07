package io.cynthia.core;

@FunctionalInterface
public interface Lambda<T> {
    T process(Request request, Model model);
}
