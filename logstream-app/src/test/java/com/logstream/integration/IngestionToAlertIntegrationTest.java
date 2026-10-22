
package com.logstream.integration;

import com.logstream.alerting.evaluator.AlertEvaluator;
import com.logstream.alerting.rule.AlertRule;
import com.logstream.alerting.webhook.SimulatedWebhook;
import com.logstream.common.proto.IngestResponse;
import com.logstream.common.proto.LogLevel;
import com.logstream.common.proto.LogMessage;
import com.logstream.common.search.LogQuery;
import com.logstream.common.search.LogSearchService;
import com.logstream.ingestion.service.IngestionService;
import com.logstream.ingestion.lucene.LuceneIndexManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IngestionToAlertIntegrationTest {

    @Autowired
    private IngestionService ingestionService;

    @Autowired
    private LogSearchService searchService;

    @Autowired
    private LuceneIndexManager index;

    @Test
    void ingestedErrorLogsTriggerAlertAndWebhook() throws Exception {
        String serviceName = "e2e-" + UUID.randomUUID();

        IngestResponse response = ingestionService.ingest(
                List.of(
                        LogMessage.newBuilder()
                                .setLevel(LogLevel.ERROR)
                                .setService(serviceName)
                                .setMessage("Payment failure one")
                                .build(),
                        LogMessage.newBuilder()
                                .setLevel(LogLevel.ERROR)
                                .setService(serviceName)
                                .setMessage("Payment failure two")
                                .build()
                ),
                0
        );

        assertThat(response.getAccepted()).isEqualTo(2);
        assertThat(response.getRejected()).isZero();

        index.refreshNow();

        long errorCount = searchService.count(
                LogQuery.all()
                        .withLevels(Set.of("ERROR"))
                        .withServices(Set.of(serviceName))
        );

        assertThat(errorCount).isEqualTo(2);

        AlertRule rule = new AlertRule(
                "E2E Error Alert",
                "ERROR",
                1
        );

        AlertEvaluator evaluator = new AlertEvaluator();
        SimulatedWebhook webhook = new SimulatedWebhook();

        boolean triggered = evaluator.shouldTrigger(rule, (int) errorCount);

        assertThat(triggered).isTrue();

        if (triggered) {
            webhook.send("Alert triggered: " + rule.getName());
        }

        assertThat(webhook.getLastMessage())
                .isEqualTo("Alert triggered: E2E Error Alert");
    }
}

