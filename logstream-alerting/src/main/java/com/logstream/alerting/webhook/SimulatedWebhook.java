package com.logstream.alerting.webhook;

public class SimulatedWebhook {

    public void send(String message) {
        System.out.println("Webhook triggered: " + message);
    }
}
