package io.cynthia.server.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true)
@Data
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProcessingOptions {
    float threshold;
    int top;
}
