package com.logstream.alerting.evaluator;

import com.logstream.alerting.rule.AlertRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AlertEvaluatorTest {

    @Test
    void shouldTriggerWhenLogCountExceedsThreshold() {

        AlertRule rule = new AlertRule(
                "Error Alert",
                "ERROR",
                100
        );

        AlertEvaluator evaluator = new AlertEvaluator();

        assertTrue(evaluator.shouldTrigger(rule, 120));
    }

    @Test
    void shouldNotTriggerWhenLogCountIsBelowThreshold() {

        AlertRule rule = new AlertRule(
                "Error Alert",
                "ERROR",
                100
        );

        AlertEvaluator evaluator = new AlertEvaluator();

        assertFalse(evaluator.shouldTrigger(rule, 80));
    }

    @Test
    void shouldNotTriggerWhenLogCountEqualsThreshold() {

        AlertRule rule = new AlertRule(
                "Error Alert",
                "ERROR",
                100
        );

        AlertEvaluator evaluator = new AlertEvaluator();

        assertFalse(evaluator.shouldTrigger(rule, 100));
    }
}