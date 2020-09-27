package io.cynthia.core.async;

import io.cynthia.core.learning.Statistics;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Metrics {
    private static final int DURATION_WINDOW = 50;

    private static final Map<String, Deque<Double>> taskDurations = new HashMap<>();

    public static void updateDuration(final Worker<?> worker) {
        taskDurations.computeIfAbsent(worker.name(), taskName -> new ArrayDeque<>(DURATION_WINDOW)).add(worker.duration());
    }

    public static double averageDuration(final Worker<?> worker) {
        final String workerName = worker.name();
        if (taskDurations.containsKey(workerName))
            return Statistics.average(taskDurations.get(workerName));
        return 0;
    }
}
