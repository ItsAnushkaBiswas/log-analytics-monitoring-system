package com.logstream.alerting.rule;

public class AlertRule {
    private String name;
    private String query;
    private int threshold;

    public AlertRule(String name, String query, int threshold) {
        this.name = name;
        this.query = query;
        this.threshold = threshold;
    }

    public String getName() {
        return name;
    }

    public String getQuery() {
        return query;
    }

    public int getThreshold() {
        return threshold;
    }
}
