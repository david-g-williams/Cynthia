package io.cynthia.core.async;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@NoArgsConstructor
@Slf4j
public class Topology {
    private WorkerPool workerPool;

    public void process() {
        WorkerPool cursorPool = workerPool;
        while (cursorPool != null) {
            cursorPool.process();
            if (workerPool.next() != null) {
                cursorPool = workerPool.next().get();
            } else {
                break;
            }
        }
    }
}
