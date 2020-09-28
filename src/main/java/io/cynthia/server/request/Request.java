package io.cynthia.server.request;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class Request {
    private List<Map<String, Object>> query;
    private String modelId;
}
