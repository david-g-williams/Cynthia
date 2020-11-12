package io.cynthia.server.request;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor
@Value
public class ProcessingOptions {
    float threshold;
    int top;
}
