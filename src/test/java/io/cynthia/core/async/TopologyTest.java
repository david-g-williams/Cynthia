package io.cynthia.core.async;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class TopologyTest {

    @Test
    public void testTopology() {
        final Worker<Object> workerA = Worker.builder().name("a").lambda(() -> "a").parallel(true).build();
        final Worker<Object> workerB = Worker.builder().name("b").lambda(() -> "b").parallel(true).build();
        final Worker<Object> workerC = Worker.builder().name("c")
            .lambda(() -> workerA.result().toString() + workerB.result().toString())
            .after(Set.of(workerA, workerB))
            .build();

        final Scheduler scheduler = Scheduler.builder().name("s").workers(Set.of(workerA, workerB, workerC)).build();

        final Topology topology = Topology.builder().scheduler(scheduler).build();

        topology.process();

        Assert.assertEquals(workerC.result(), "ab");
    }
}
