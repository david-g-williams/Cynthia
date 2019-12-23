package io.cynthia.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Accessors(fluent = true, chain = true)
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Response {
    private String modelId;
    private List<Map<String, Object>> results;
}
