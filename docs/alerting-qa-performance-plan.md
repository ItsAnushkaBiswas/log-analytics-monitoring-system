# Alerting QA & Performance Plan

## 1. Alert Rules and Thresholds

The alerting module evaluates log counts against configured thresholds and triggers a simulated webhook notification when the matching log count exceeds the configured threshold.

| Alert Rule | Query / Log Level | Threshold |
|---|---|---:|
| Error Alert | ERROR | 100 |
| Warning Alert | WARN | 200 |
| Critical Alert | CRITICAL | 50 |

**Threshold behaviour:** An alert is triggered when the matching log count is strictly greater than the configured threshold. A count equal to or below the threshold does not trigger an alert.

## 2. QA Plan

The alerting system is verified through the following checks:

1. **Alert rule creation:** Verify that alert rules contain the expected name, query, and threshold.
2. **Threshold evaluation:** Verify that an alert triggers when the matching log count exceeds the configured threshold.
3. **No false trigger:** Verify that an alert does not trigger when the matching log count is below the threshold.
4. **Threshold boundary:** Verify the expected behaviour when the matching log count equals the threshold.
5. **Scheduler execution:** Verify that the scheduled alert-check method executes correctly.
6. **Webhook triggering:** Verify that a triggered alert produces the expected simulated webhook notification.
7. **Lucene integration:** Verify that the scheduler obtains matching log counts from the actual `LogSearchService` rather than using a hardcoded simulated count.
8. **Ingestion-to-alert regression:** Verify that ingested ERROR logs are indexed, counted through Lucene, evaluated against an alert threshold, and used to trigger the simulated webhook.
9. **Search and aggregation regression:** Verify log retrieval, filtering, total count, count-by-field, and histogram functionality.
10. **gRPC and Live Tail regression:** Verify gRPC ingestion and WebSocket connectivity using the corresponding integration tests and manual checks.

## 3. Performance Plan

The following performance aspects were measured or considered during project testing:

1. **Alert evaluation time:** Measure the time required to evaluate an alert rule.
2. **Lucene count performance:** Measure the time required to count matching log records.
3. **Webhook response time:** Measure the execution time of the simulated webhook.
4. **Scheduler execution:** Verify that alert checks execute correctly and evaluate the retrieved log count.
5. **Multiple-rule performance:** Consider scheduler behaviour when multiple alert rules are configured.
6. **Load behaviour:** Evaluate ingestion, search, aggregation, and alerting performance as the log volume increases.

## 4. Performance Evidence

The following benchmark results were recorded during project testing.

| Benchmark | Measured Result |
|---|---:|
| Alert evaluation | 31.926 ns per evaluation |
| Simulated webhook call | 7,519.813 ns per call |
| Lucene `count()` | 51,146 ns per operation |
| Lucene `countBy()` | 214,934 ns per operation |
| Lucene `histogram()` | 1,140,418 ns per operation |
| Ingestion throughput | 88,513 logs/sec |
| Ingestion time | 11.30 sec for 1,000,000 logs |
| Index refresh time | 1,477 ms |
| Keyword search on 1M-log benchmark | 17.3 ms per operation |
| Filter search | 1.9 ms per operation |
| Ingestion benchmark index size | 1,080,000 documents |

**Performance interpretation:** These measurements provide baseline performance evidence for the alerting, search, aggregation, and ingestion components. They represent results from the recorded test environment and should not be interpreted as guaranteed production performance.

## 5. End-to-End Regression Testing

### 5.1 Objective

Verify that the integrated Log Analytics Monitoring System behaves correctly across ingestion, indexing, search, aggregation, alert evaluation, webhook notification, and WebSocket communication.

### 5.2 Test Coverage

- **gRPC ingestion:** Verify that logs can be ingested through the gRPC service.
- **Indexing and search:** Verify that ingested logs can be retrieved and filtered by log level.
- **Aggregation:** Verify total log counts, counts grouped by a field, and histogram results.
- **Alert evaluation:** Verify threshold behaviour, including below-threshold and boundary cases.
- **Real Lucene scheduler integration:** Verify that the scheduler obtains the matching log count from `LogSearchService`.
- **Simulated webhook:** Verify that a triggered alert produces the expected notification.
- **Live Tail:** Verify that the WebSocket endpoint accepts a client connection and that live logs can be displayed in the frontend dashboard.
- **Full build regression:** Run the Maven reactor test suite across the project modules.

### 5.3 Automated Ingestion-to-Alert Regression Test

The `IngestionToAlertIntegrationTest` was added and executed successfully.

The test performs the following operations:

1. Ingests two ERROR logs through `IngestionService`.
2. Verifies that both logs are accepted and none are rejected.
3. Refreshes the Lucene index.
4. Queries Lucene to confirm that the matching ERROR log count is two.
5. Evaluates the alert rule against the retrieved count.
6. Verifies that the simulated webhook receives the expected alert message.

**Test result:** 1 test passed, 0 failed.

**Scope:** This test validates the ingestion-to-alert logic using the real Lucene search/count service, the alert evaluator, and a simulated webhook. It invokes the ingestion service directly rather than exercising the network gRPC endpoint or the Spring scheduled execution path. Those areas are covered by separate tests and checks.

### 5.4 Real Lucene Scheduler Integration

The `AlertScheduler` was updated to replace the hardcoded simulated log count with a count retrieved from the injected `LogSearchService`.

The scheduler now:

1. Builds a `LogQuery` using the alert rule's configured log-level query.
2. Retrieves the matching log count from Lucene through `LogSearchService`.
3. Passes the actual count to `AlertEvaluator`.
4. Triggers the simulated webhook when the configured threshold is exceeded.
5. Logs the alert name, query, threshold, actual log count, and trigger result.

The scheduler is configured to execute at a fixed rate of 60 seconds.

**Test result:** The updated scheduler test passed, and the full Maven reactor test suite completed successfully.

**Implementation scope:** The current implementation interprets `AlertRule.getQuery()` as a log-level value, such as `ERROR`. The scheduler's query construction and evaluation have been tested with the current implementation.

## 6. Current QA Status

| Component / Activity | Result |
|---|---|
| Alert rule and threshold evaluation tests | Passed |
| Below-threshold and threshold-boundary evaluator tests | Passed |
| Scheduler and simulated webhook tests | Passed |
| Real Lucene count integration in scheduler | Implemented; tests passed |
| Ingestion-to-alert regression test | Passed: 1/1 |
| gRPC ingestion integration tests | Passed in the recorded test run |
| Search and aggregation checks | Verified in the recorded test run |
| Live Tail WebSocket and frontend integration | Previously verified |
| Full Maven reactor test suite | **BUILD SUCCESS** |

## 7. Final Validation and Acceptance

The alerting module has been integrated with the Lucene search service, replacing the previously hardcoded log count with the actual count of matching log records. The scheduler evaluates the retrieved count against the configured threshold and triggers the simulated webhook when the threshold is exceeded.

The alert evaluator, scheduler, simulated webhook, and ingestion-to-alert regression test have been executed successfully. The full Maven reactor test suite completed with **BUILD SUCCESS** across all project modules.

Performance benchmarks have also been recorded for alert evaluation, simulated webhook execution, Lucene search and aggregation, and log ingestion. Search, aggregation, gRPC ingestion, and Live Tail functionality have been verified through the corresponding automated tests and manual checks.

These results provide evidence that the implemented alerting and regression-testing components are functioning as expected within the tested environment.
