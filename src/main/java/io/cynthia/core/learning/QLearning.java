package io.cynthia.core.learning;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import static io.cynthia.Constants.EPSILON;
import static io.cynthia.Constants.INFINITY;
import static io.cynthia.core.learning.Statistics.argMax;
import static io.cynthia.core.learning.Statistics.sample;

@Accessors(fluent = true)
@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QLearning {
    double discountRate = 1.0;
    double explorationRate = 1.0;
    double explorationRateDecay = 0.999;
    double learningRate = 0.01;
    double learningRateDecay = 1.0;
    int[] actions = new int[0];
    Map<String, Double> explorationRates = new ConcurrentHashMap<>();
    Map<String, double[]> qValues = new ConcurrentHashMap<>();

    private double bellmanUpdate(final double qValue, final double nextQValue, final double reward) {
        return (1 - learningRate) * qValue + learningRate * (reward + discountRate * nextQValue);
    }

    private double[] epsilonGreedyWeights(final String state) {
        final double explorationRate = explorationRates.getOrDefault(state, this.explorationRate);
        final double[] samplingWeights = new double[actions.length];
        Arrays.fill(samplingWeights, explorationRate / actions.length);
        final double[] qValues = this.qValues.getOrDefault(state, new double[actions.length]);
        samplingWeights[argMax(qValues)] = 1 - explorationRate + explorationRate / actions.length;
        return samplingWeights;
    }

    @Synchronized
    public void learn(final Observation observation) {
        final double reward = observation.reward();
        final int action = observation.action();
        final String nextState = observation.nextState();
        final String state = observation.state();

        if(!explorationRates.containsKey(state)) {
            explorationRates.put(state, this.explorationRate);
        } else {
            if(explorationRates.get(state) < EPSILON) {
                return;
            }
        }

        if(!qValues.containsKey(state)) {
            qValues.put(state, new double[actions.length]);
        }

        if(observation.done()) {
            final double currentQValue = qValues.get(state)[action];
            qValues.get(state)[action] = bellmanUpdate(currentQValue, 0, reward);
        } else {
            final int nextGreedyAction;
            if (qValues.containsKey(nextState))
                nextGreedyAction = argMax(qValues.get(nextState));
            else {
                nextGreedyAction = (int) (Math.random() * actions.length);
                qValues.put(nextState, new double[actions.length]);
            }

            qValues.get(state)[action] = bellmanUpdate(
                qValues.get(state)[action],
                qValues.get(nextState)[nextGreedyAction],
                reward
            );
        }

        learningRate = learningRate * learningRateDecay;
        explorationRates.put(state, explorationRates.get(state) * explorationRateDecay);
    }

    public int nextAction(final String state) {
        if (explorationRates == null) explorationRates = new ConcurrentHashMap<>();
        if (qValues == null) qValues = new ConcurrentHashMap<>();

        if(explorationRates.getOrDefault(state, INFINITY) < EPSILON) {
            return argMax(qValues.get(state));
        }
        return sample(epsilonGreedyWeights(state), actions);
    }
}
