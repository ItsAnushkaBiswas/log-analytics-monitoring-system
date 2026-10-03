package com.logstream.alerting.webhook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SimulatedWebhookTest {

    @Test
    void shouldTriggerWebhook() {
        SimulatedWebhook webhook = new SimulatedWebhook();

        assertDoesNotThrow(() -> webhook.send("Test alert"));
    }
}