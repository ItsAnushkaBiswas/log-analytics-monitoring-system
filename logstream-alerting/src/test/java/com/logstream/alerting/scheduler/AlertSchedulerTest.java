package com.logstream.alerting.scheduler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class AlertSchedulerTest {
    @Test
    void schedulerTest() {
        AlertScheduler scheduler = new AlertScheduler();

        assertDoesNotThrow(() -> scheduler.runAlerts());
    }
}
