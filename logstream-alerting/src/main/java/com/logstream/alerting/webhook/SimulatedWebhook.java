package com.logstream.alerting.webhook;

public class SimulatedWebhook {
    private String lastMessage;

    public void send(String message) {
        lastMessage = message;
        System.out.println("Webhook triggered: " + message);
    }
    public String getLastMessage() {
        return lastMessage;
    }
}
