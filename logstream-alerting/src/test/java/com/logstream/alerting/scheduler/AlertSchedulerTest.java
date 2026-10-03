package com.logstream.alerting.scheduler;
import org.junit.jupiter.api.Test;

public class AlertSchedulerTest {
    @Test
    void schedulerTest() {
        AlertScheduler scheduler = new AlertScheduler();
        scheduler.runAlerts();
    }
}
