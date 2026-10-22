
package com.logstream.alerting.evaluator;

import com.logstream.alerting.rule.AlertRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertEvaluatorTest {

    @Test
    void shouldNotTriggerWhenLogCountIsBelowThreshold() {
        AlertRule rule = new AlertRule("Error Alert", "ERROR", 100);
        AlertEvaluator evaluator = new AlertEvaluator();

        assertFalse(evaluator.shouldTrigger(rule, 99));
    }

    @Test
    void shouldTriggerWhenLogCountEqualsThreshold() {
        AlertRule rule = new AlertRule("Error Alert", "ERROR", 100);
        AlertEvaluator evaluator = new AlertEvaluator();

        assertTrue(evaluator.shouldTrigger(rule, 100));
    }
}
