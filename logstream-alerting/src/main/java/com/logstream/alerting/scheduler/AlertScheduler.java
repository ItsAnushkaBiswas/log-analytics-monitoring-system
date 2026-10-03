package com.logstream.alerting.scheduler;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class AlertScheduler {
    @Scheduled(fixedRate = 60000)
    public void runAlerts() {
        System.out.println("Running scheduled alert check...");
    }
}
