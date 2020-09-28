package io.cynthia.core.learning;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.Well512a;

import java.util.Collection;
import java.util.Deque;

import static io.cynthia.Constants.EPSILON;
import static io.cynthia.Constants.INFINITY;

public final class Statistics {

    public static int argMax(final double[] input) {
        int maxIndex = 0;
        double maxValue = -1.0 * INFINITY;
        for(int i = 0; i < input.length; i++) {
            if(input[i] > maxValue) {
                maxIndex = i;
                maxValue = input[i];
            }
        }
        return maxIndex;
    }

    public static double sparseCategoricalCrossEntropy(final int index, final double[] values) {
        if (values[index] < EPSILON) {
            return -1.0 * Math.log(EPSILON);
        }
        return -1.0 * Math.log(values[index]);
    }

    public static double updateAverage(final double nextValue, final double currentAverage, final long currentCount) {
        return currentAverage + (nextValue - currentAverage) / (currentCount + 1);
    }

    public static double average(final Collection<Double> values) {
        double average = 0.0;
        long currentCount = 0;
        for (final Double value : values)
            average = updateAverage(value, average, currentCount++);
        return average;
    }

    public static int sample(final double[] weights, final int[] items) {
        return new EnumeratedIntegerDistribution(new Well512a(), items, weights).sample();
    }
}
