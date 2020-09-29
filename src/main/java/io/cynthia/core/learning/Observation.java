package io.cynthia.core.learning;

import java.util.Map;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true)
@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Observation {
    boolean done;
    double reward;
    int action;
    Map<String, Object> info;
    String nextState;
    String state;
}
