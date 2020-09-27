package io.cynthia.core.async;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

import io.cynthia.core.learning.Observation;
import io.cynthia.core.learning.QLearning;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static io.cynthia.Constants.*;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@NoArgsConstructor
@Slf4j
public class WorkerPool {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
    private static final ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;

    private String name;
    private Set<Worker<?>> workers;
    private Supplier<WorkerPool> next;

    private final QLearning qLearning = QLearning
        .actions(2)
        .alpha(0.01)
        .gamma(0.99)
        .epsilon(1.0)
        .alphaDecay(0.999)
        .epsilonDecay(0.999);

    private String getSystemState() {
        final int activeThreadCount = threadPoolExecutor.getActiveCount();
        final int maximumThreadCount = threadPoolExecutor.getMaximumPoolSize();
        final int waitingThreadCount = threadPoolExecutor.getQueue().size();
        final long freeMemory = Runtime.getRuntime().freeMemory();
        final String name = this.name();
        final String discreteMemoryRatio = String.format("%3.0f", 100.0 * freeMemory / TOTAL_MEMORY);
        final String discreteUsageRatio = String.format("%3.0f", 100.0 * activeThreadCount / maximumThreadCount);
        final boolean hasWaiting = waitingThreadCount > 0;
        return String.format("%s__%s__%s__%s", name, discreteMemoryRatio, discreteUsageRatio, hasWaiting);
    }

    public void process() {
        final Deque<Worker<?>> workerDeque = new ArrayDeque<>(workers);

        final List<CompletableFuture<?>> completableFutures = new ArrayList<>();

        final String state = getSystemState();
        final int action = qLearning.nextAction(state);

        final long start = System.nanoTime();
        try {
            while (workerDeque.size() > 0) {
                final List<Worker<?>> ready = new ArrayList<>();

                while (workerDeque.size() > 0) {
                    final Worker<?> worker = workerDeque.poll();
                    if(worker.isReady()) {
                        ready.add(worker);
                    } else {
                        workerDeque.add(worker);
                    }
                }

                if (ready.size() > 0) {
                    int maxIndex = 0;
                    double maxDuration = -1.0 * INFINITY;
                    for (int i = 0; i < ready.size(); i++) {
                        final Worker<?> worker = ready.get(i);
                        final double workerDuration = Metrics.averageDuration(worker);
                        if (workerDuration > maxDuration) {
                            maxIndex = i;
                            maxDuration = workerDuration;
                        }
                    }

                    final Worker<?> worker = ready.get(maxIndex);

                    workerDeque.addAll(ready.subList(0, maxIndex));
                    workerDeque.addAll(ready.subList(maxIndex + 1, ready.size()));

                    if (maxDuration > 10 && action == 1) {
                        worker.parallel(true);
                    } else {
                        worker.parallel(false);
                    }

                    if(worker.parallel()) {
                        completableFutures.add(
                            CompletableFuture.supplyAsync(() -> {
                                worker.process();
                                return true;
                            }, executorService));
                    } else {
                        worker.process();
                    }
                }
            }

            final CompletableFuture<?>[] futureArray = completableFutures.toArray(new CompletableFuture[0]);

            if(futureArray.length > 0)
                CompletableFuture.allOf(futureArray).join();

            final double reward = -1.0 * (System.nanoTime() - start) / 1e6;

            final Observation observation = Observation.builder()
                .state(state)
                .action(action)
                .reward(reward)
                .done(true)
                .build();

            qLearning.learn(observation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}