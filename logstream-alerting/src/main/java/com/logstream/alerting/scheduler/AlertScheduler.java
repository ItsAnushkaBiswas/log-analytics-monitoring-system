package com.logstream.alerting.scheduler;

import com.logstream.alerting.webhook.SimulatedWebhook;
import com.logstream.alerting.evaluator.AlertEvaluator;
import com.logstream.alerting.rule.AlertRule;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class AlertScheduler {

    private final AlertRule alertRule;
    private final AlertEvaluator evaluator = new AlertEvaluator();
    private final SimulatedWebhook webhook = new SimulatedWebhook();

    public AlertScheduler(AlertRule alertRule) {
        this.alertRule = alertRule;
    }

    @Scheduled(fixedRate = 60000)
    public void runAlerts() {

        int logCount = 120;

        boolean triggered = evaluator.shouldTrigger(alertRule, logCount);
        if (triggered) {
            webhook.send("Alert triggered: " + alertRule.getName());
        }

        System.out.println(
                "Checking alert: " + alertRule.getName()
                        + " | Query: " + alertRule.getQuery()
                        + " | Threshold: " + alertRule.getThreshold()
                        + " | Log Count: " + logCount
                        + " | Triggered: " + triggered
        );
    }
}
