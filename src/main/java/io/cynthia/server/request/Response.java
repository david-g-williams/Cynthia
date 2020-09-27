package io.cynthia.server.request;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class Response {
    private List<?> results;
    private String modelId;
}
