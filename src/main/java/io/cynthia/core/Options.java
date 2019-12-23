package io.cynthia.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Options {
    private int top;
    private float threshold;
}
