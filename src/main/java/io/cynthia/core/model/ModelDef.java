package io.cynthia.core.model;

import lombok.*;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class ModelDef {
    private String location;
}