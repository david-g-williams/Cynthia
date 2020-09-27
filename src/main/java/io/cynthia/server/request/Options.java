package io.cynthia.server.request;

import lombok.*;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class Options {
    private float threshold;
    private int top;
}
