package io.cynthia.core.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@Data
@FieldDefaults(level= AccessLevel.PRIVATE)
public class LabelScore {
    String label;
    double score;
}
