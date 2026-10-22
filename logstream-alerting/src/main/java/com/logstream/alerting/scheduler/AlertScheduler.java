
package com.logstream.alerting.scheduler;

import com.logstream.alerting.webhook.SimulatedWebhook;
import com.logstream.alerting.evaluator.AlertEvaluator;
import com.logstream.alerting.rule.AlertRule;
import com.logstream.common.search.LogQuery;
import com.logstream.common.search.LogSearchService;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@EnableScheduling
@Component
public class AlertScheduler {

    private final AlertRule alertRule;
    private final AlertEvaluator evaluator = new AlertEvaluator();
    private final SimulatedWebhook webhook = new SimulatedWebhook();
    private final LogSearchService searchService;

    public AlertScheduler(
            AlertRule alertRule,
            LogSearchService searchService) {
        this.alertRule = alertRule;
        this.searchService = searchService;
    }

    @Scheduled(fixedRate = 60000)
    public void runAlerts() {

        LogQuery query = LogQuery.all()
                .withLevels(Set.of(alertRule.getQuery()));

        long logCount = searchService.count(query);
        int matchingLogCount = Math.toIntExact(logCount);

        boolean triggered = evaluator.shouldTrigger(
                alertRule, matchingLogCount);

        if (triggered) {
            webhook.send(
                    "Alert triggered: " + alertRule.getName());
        }

        System.out.println(
                "Checking alert: " + alertRule.getName()
                        + " | Query: " + alertRule.getQuery()
                        + " | Threshold: " + alertRule.getThreshold()
                        + " | Actual Log Count: " + logCount
                        + " | Triggered: " + triggered
        );
    }

    public String getLastWebhookMessage() {
        return webhook.getLastMessage();
    }
}
