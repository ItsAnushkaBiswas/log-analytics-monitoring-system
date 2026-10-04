package com.logstream.alerting.evaluator;

import com.logstream.alerting.rule.AlertRule;

public class AlertEvaluator {

    public boolean shouldTrigger(AlertRule rule, int logCount) {
        return logCount > rule.getThreshold();
    }
}
