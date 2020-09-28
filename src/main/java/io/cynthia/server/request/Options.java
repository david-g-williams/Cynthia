package io.cynthia.server.request;

import lombok.*;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class Options {
    float threshold;
    int top;
}
