package com.logstream.alerting.evaluator;

import com.logstream.alerting.rule.AlertRule;
import org.junit.jupiter.api.Test;

public class AlertEvaluatorPerformanceTest {

    @Test
    void measureAlertEvaluationTime() {

        AlertRule rule = new AlertRule(
                "Error Alert",
                "ERROR",
                100
        );

        AlertEvaluator evaluator = new AlertEvaluator();

        int iterations = 100_000;

        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            evaluator.shouldTrigger(rule, 120);
        }

        long endTime = System.nanoTime();

        long totalTimeNs = endTime - startTime;
        double averageTimeNs = (double) totalTimeNs / iterations;

        System.out.println("Alert evaluation benchmark");
        System.out.println("Iterations: " + iterations);
        System.out.println("Total time (ns): " + totalTimeNs);
        System.out.println("Average time per evaluation (ns): " + averageTimeNs);
    }
}
