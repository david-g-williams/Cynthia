package io.cynthia.core.async;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static io.cynthia.core.learning.Statistics.average;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Metrics {
    static int MOVING_AVERAGE_SIZE = 50;

    static Map<String, Deque<Double>> movingAverages = new HashMap<>();

    public static void appendMovingAverage(final String name, final double value) {
        if (!movingAverages.containsKey(name)) {
            movingAverages.put(name, new ArrayDeque<>(MOVING_AVERAGE_SIZE));
        }
        movingAverages.get(name).add(value);
    }

    public static double getMovingAverage(final String name) {
        if (movingAverages.containsKey(name)) {
            return average(movingAverages.get(name));
        }
        return 0;
    }
}
