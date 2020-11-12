package io.cynthia.core.model;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor
@Value
public class ModelDef {
    String modelId;
    String location;
    double gpuFraction;
}
