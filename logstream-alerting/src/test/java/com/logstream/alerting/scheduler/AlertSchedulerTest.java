package com.logstream.alerting.scheduler;

import com.logstream.alerting.rule.AlertRule;
import org.junit.jupiter.api.Test;


public class AlertSchedulerTest {
    @Test
    void schedulerTest() {
        AlertRule rule = new AlertRule("Error Alert", "ERROR", 100);
        AlertScheduler scheduler = new AlertScheduler(rule);

        scheduler.runAlerts();
    }
}
