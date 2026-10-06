package com.logstream.alerting.webhook;

import org.junit.jupiter.api.Test;

public class SimulatedWebhookPerformanceTest {

    @Test
    void measureWebhookResponseTime() {

        SimulatedWebhook webhook = new SimulatedWebhook();

        int iterations = 100_000;

        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            webhook.send("Performance test alert");
        }

        long endTime = System.nanoTime();

        long totalTimeNs = endTime - startTime;
        double averageTimeNs = (double) totalTimeNs / iterations;

        System.out.println("Webhook response-time benchmark");
        System.out.println("Iterations: " + iterations);
        System.out.println("Total time (ns): " + totalTimeNs);
        System.out.println("Average time per webhook call (ns): " + averageTimeNs);
    }
}
