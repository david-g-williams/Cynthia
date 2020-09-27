package io.cynthia.core.learning;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.*;
import lombok.experimental.Accessors;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.Well512a;

import static io.cynthia.Constants.EPSILON;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class QLearning {
    private double alpha = 0.01;
    private double alphaDecay = 1.0;
    private double epsilon = 1.0;
    private double epsilonDecay = 0.999;
    private double gamma = 1.0;
    private int[] actions;
    private int nActions;
    private Map<String, double[]> qValues = new HashMap<>();
    private Map<String, Double> stateEpsilons = new HashMap<>();

    public static QLearning actions(final int nActions) {
        final QLearning qLearning = new QLearning();
        qLearning.nActions = nActions;
        qLearning.actions = new int[nActions];
        for(int i = 0; i < nActions; i++)
            qLearning.actions[i] = i;
        return qLearning;
    }

    private double bellmanUpdate(final double qValue, final double nextQValue, final double reward) {
        return (1 - alpha) * qValue + alpha * (reward + gamma * nextQValue);
    }

    private int sample(final double[] weights) {
        return new EnumeratedIntegerDistribution(new Well512a(), actions, weights).sample();
    }

    private double[] epsilonGreedyWeights(final String state) {
        final double[] samplingWeights = new double[nActions];
        final double explorationRate = stateEpsilons.getOrDefault(state, this.epsilon);
        final double[] qValues = this.qValues.getOrDefault(state, new double[nActions]);
        Arrays.fill(samplingWeights, explorationRate / nActions);
        samplingWeights[Statistics.argMax(qValues)] = 1 - explorationRate + explorationRate / nActions;
        return samplingWeights;
    }

    public int nextAction(final String state) {
        if(stateEpsilons.getOrDefault(state, Double.MAX_VALUE) < EPSILON) {
            return Statistics.argMax(qValues.get(state));
        }
        final double[] samplingWeights = epsilonGreedyWeights(state);
        return sample(samplingWeights);
    }

    public synchronized void learn(final Observation observation) {
        final String state = observation.state();
        final int action = observation.action();
        final double reward = observation.reward();
        final String nextState = observation.nextState();

        if(!stateEpsilons.containsKey(state)) {
            stateEpsilons.put(state, this.epsilon);
        } else {
            if(stateEpsilons.get(state) < EPSILON) {
                return;
            }
            stateEpsilons.put(state, stateEpsilons.get(state) * epsilonDecay);
        }

        if(!qValues.containsKey(state)) {
            qValues.put(state, new double[nActions]);
        }

        if(observation.done()) {
            final double currentQValue = qValues.get(state)[action];
            qValues.get(state)[action] = bellmanUpdate(currentQValue, 0, reward);
        } else {
            final int nextGreedyAction;
            if (qValues.containsKey(nextState))
                nextGreedyAction = Statistics.argMax(qValues.get(nextState));
            else {
                nextGreedyAction = (int) (Math.random() * nActions);
                qValues.put(nextState, new double[nActions]);
            }

            qValues.get(state)[action] = bellmanUpdate(
                qValues.get(state)[action],
                qValues.get(nextState)[nextGreedyAction],
                reward
            );
        }

        alpha = alpha * alphaDecay;
    }
}
