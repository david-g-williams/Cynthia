package io.cynthia.core.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@Data
@FieldDefaults(level= AccessLevel.PRIVATE)
@NoArgsConstructor
public class ModelDef {
    String location;
    double gpuFraction = 1.0;
}
