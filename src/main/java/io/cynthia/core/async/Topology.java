package io.cynthia.core.async;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Topology {
    WorkerPool workerPool;

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
