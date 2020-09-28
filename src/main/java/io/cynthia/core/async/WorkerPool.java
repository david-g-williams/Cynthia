package io.cynthia.core.async;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.cynthia.core.learning.Observation;
import io.cynthia.core.learning.QLearning;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import static io.cynthia.Constants.*;

@Accessors(fluent = true)
@AllArgsConstructor
@Builder
@FieldDefaults(makeFinal=true, level= AccessLevel.PRIVATE)
@Slf4j
public class WorkerPool {
    static ExecutorService executorService = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
    static ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;

    String name;
    Set<Worker<?>> workers;
    @Getter Supplier<WorkerPool> next;

    private final QLearning qLearning = QLearning.builder()
        .actions(new int[] {0, 1})
        .discountRate(0.99)
        .explorationRate(1.0)
        .explorationRateDecay(0.999)
        .learningRate(0.01)
        .learningRateDecay(0.999)
        .build();

    private String getSystemState() {
        final int activeThreadCount = threadPoolExecutor.getActiveCount();
        final int maximumThreadCount = threadPoolExecutor.getMaximumPoolSize();
        final int waitingThreadCount = threadPoolExecutor.getQueue().size();
        final long freeMemory = Runtime.getRuntime().freeMemory();
        final String discreteMemoryRatio = String.format("%3.0f", 100.0 * freeMemory / TOTAL_MEMORY);
        final String discreteUsageRatio = String.format("%3.0f", 100.0 * activeThreadCount / maximumThreadCount);
        final boolean hasWaiting = waitingThreadCount > 0;
        return String.format("%s__%s__%s", discreteMemoryRatio, discreteUsageRatio, hasWaiting);
    }

    public void process() {
        final Deque<Worker<?>> workerDeque = new ArrayDeque<>(workers);

        final List<CompletableFuture<?>> completableFutures = new ArrayList<>();

        final String state = getSystemState();
        final int action = qLearning.nextAction(state);

        final long start = System.nanoTime();

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
                    final double workerDuration = Metrics.averageWorkerDuration(worker);
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
                        }, executorService).thenAccept(completed -> {
                            if (completed) {
                                Metrics.updateWorkerDuration(worker);
                            }
                        }));
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

    }
}