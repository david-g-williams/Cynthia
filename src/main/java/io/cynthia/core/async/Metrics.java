package io.cynthia.core.async;

import io.cynthia.core.learning.Statistics;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Metrics {
    static int DURATION_WINDOW = 50;

    static Map<String, Deque<Double>> workerDurations = new HashMap<>();

    public static void updateWorkerDuration(final Worker<?> worker) {
        workerDurations.computeIfAbsent(worker.name(), taskName -> new ArrayDeque<>(DURATION_WINDOW)).add(worker.duration());
    }

    public static double averageWorkerDuration(final Worker<?> worker) {
        final String workerName = worker.name();
        if (workerDurations.containsKey(workerName)) {
            return Statistics.average(workerDurations.get(workerName));
        }
        return 0;
    }
}
