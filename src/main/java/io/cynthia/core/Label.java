package io.cynthia.core;

import lombok.*;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Label {
    private String label;
    private Float score;
}
