package com.logstream.alerting.rule;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlertRuleTest {

    @Test
    void shouldCreateAlertRule() {

        AlertRule rule = new AlertRule(
                "Error Alert",
                "ERROR",
                100
        );

        assertEquals("Error Alert", rule.getName());
        assertEquals("ERROR", rule.getQuery());
        assertEquals(100, rule.getThreshold());
    }
}
