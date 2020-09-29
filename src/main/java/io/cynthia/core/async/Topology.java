package io.cynthia.core.async;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Topology {
    Scheduler scheduler;

    public void process() {
        Scheduler cursorPool = scheduler;
        while (cursorPool != null) {
            cursorPool.process();
            if (scheduler.next() != null) {
                cursorPool = scheduler.next().get();
            } else {
                break;
            }
        }
    }
}
