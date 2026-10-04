package com.logstream.alerting.scheduler;

import com.logstream.alerting.rule.AlertRule;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class AlertSchedulerTest {
    @Test
    void schedulerTest() {
        AlertRule rule = new AlertRule("Error Alert", "ERROR", 100);
        AlertScheduler scheduler = new AlertScheduler(rule);

        assertDoesNotThrow(scheduler::runAlerts);
    }
}
