package io.cynthia.core;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.Map;

@Accessors(fluent = true, chain = true)
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Index {
    private Map<String, Map<String, Integer>> inputTokenToId;
    private Map<String, Map<Integer, String>> outputIdToName;
}
