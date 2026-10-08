package com.logstream.alerting;

import com.logstream.alerting.rule.AlertRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlertingConfig {

    @Bean
    public AlertRule alertRule() {
        return new AlertRule(
                "Error Alert",
                "ERROR",
                100
        );
    }
}