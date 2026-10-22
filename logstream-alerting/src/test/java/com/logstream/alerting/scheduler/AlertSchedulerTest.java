
package com.logstream.alerting.scheduler;

import static org.junit.jupiter.api.Assertions.assertNull;
import com.logstream.alerting.rule.AlertRule;
import com.logstream.common.search.LogQuery;
import com.logstream.common.search.LogSearchService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AlertSchedulerTest {

    @Test
    void schedulerTriggersWhenCountMeetsThreshold() {
        AlertRule rule = new AlertRule("Error Alert", "ERROR", 100);

        LogSearchService searchService = mock(LogSearchService.class);
        when(searchService.count(any(LogQuery.class))).thenReturn(100L);

        AlertScheduler scheduler = new AlertScheduler(rule, searchService);

        scheduler.runAlerts();

        assertEquals(
                "Alert triggered: Error Alert",
                scheduler.getLastWebhookMessage()
        );
    }

    @Test
    void schedulerDoesNotTriggerWhenCountIsBelowThreshold() {
        AlertRule rule = new AlertRule("Error Alert", "ERROR", 100);

        LogSearchService searchService = mock(LogSearchService.class);
        when(searchService.count(any(LogQuery.class))).thenReturn(99L);

        AlertScheduler scheduler = new AlertScheduler(rule, searchService);

        scheduler.runAlerts();

        assertEquals(null, scheduler.getLastWebhookMessage());
    }
}

