# Alerting QA & Performance Plan

## 1. Alert Rules and Thresholds

| Alert Rule | Query | Threshold |
|---|---|---:|
| Error Alert | ERROR | 100 |
| Warning Alert | WARN | 200 |
| Critical Alert | CRITICAL | 50 |

## 2. QA Plan

The alerting system will be verified through the following checks:

1. Alert rule creation — verify that rules are created with the correct query and threshold.
2. Threshold evaluation — verify that an alert triggers when the log count exceeds the configured threshold.
3. No false trigger — verify that an alert does not trigger when the log count is below the threshold.
4. Scheduler execution — verify that the scheduled alert check executes correctly.
5. Webhook triggering — verify that the webhook is triggered when an alert condition is met.
6. Invalid rule handling — verify that invalid alert configurations are handled safely.
7. Integration testing — verify that the alerting components work together correctly.

## 3. Performance Plan

The following performance aspects will be measured:

1. Alert evaluation time — measure the time required to evaluate an alert rule.
2. Multiple-rule performance — measure scheduler behaviour when multiple alert rules are configured.
3. Webhook response time — measure the time taken to trigger the simulated webhook.
4. Scheduler stability — verify that scheduled alert checks continue to execute reliably.
5. Load behaviour — observe alerting performance as the number of log records and alert rules increases.

## 4. Performance Evidence

| Benchmark                       | Result | Status |
|---------------------------------|---:|--------|
| Alert evaluation time           | 31.926 ns per evaluation | Passed |
| Simulated webhook call time     | 7,519.813 ns per call | Passed |
| Lucene count() average time | 51,146 ns per operation | Passed |
| Lucene countBy() average time | 214,934 ns per operation | Passed |
| Lucene histogram() average time | 1,140,418 ns per operation | Passed |
| Ingestion throughput            | 88,513 logs/sec | Passed |
| Ingestion time                  | 11.30 sec for 1,000,000 logs | Passed |
| Refresh time                    | 1,477 ms | Passed |
| 1M-log keyword search | 17.3 ms per operation | Passed |
| Filter search                   | 1.9 ms per operation | Passed | 
| Ingestion benchmark index size  | 1,080,000 docs | Passed | 