package io.cynthia.core.learning;

import java.util.Map;

import lombok.*;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class Observation {
    private boolean done;
    private double reward;
    private int action;
    private Map<String, Object> info;
    private String nextState;
    private String state;
}
