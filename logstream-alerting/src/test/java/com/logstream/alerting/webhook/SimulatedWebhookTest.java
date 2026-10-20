package com.logstream.alerting.webhook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimulatedWebhookTest {

    @Test
    void shouldTriggerWebhook() {
        SimulatedWebhook webhook = new SimulatedWebhook();

        webhook.send("Test alert");

        assertEquals("Test alert", webhook.getLastMessage());
    }
}