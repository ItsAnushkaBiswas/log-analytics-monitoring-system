package com.logstream.ingestion.grpc;

import com.logstream.common.proto.IngestResponse;
import com.logstream.common.proto.LogLevel;
import com.logstream.common.proto.LogMessage;
import com.logstream.common.search.LogField;
import com.logstream.common.search.LogQuery;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "benchmark", matches = "true")
class IngestionBenchmarkTest {
    private static final int CLIENTS = 4;
    private static final int LOGS_PER_CLIENT = 250_000;
    private static final String[] SERVICES = {"auth", "payment", "orders", "search", "inventory"};
    private static final String[] MESSAGES = {
            "user %d logged in from 10.0.%d.12",
            "payment %d declined: insufficient funds (code %d)",
            "order %d shipped to warehouse %d",
            "search query took %d ms for %d results",
            "gateway timeout calling inventory after %d ms (attempt %d)",
    };

    @TempDir
    Path dir;

    @Test
    void ingestionThroughput() throws Exception {
        try (IngestionServerFixture fx = new IngestionServerFixture(dir)) {
            fx.pushed.clear();
            send(fx, 20_000);
            fx.index.refreshNow();
            long warmup = fx.search.count(LogQuery.all());

            long start = System.nanoTime();
            long accepted = send(fx, LOGS_PER_CLIENT);
            double ingestSec = (System.nanoTime() - start) / 1e9;

            long refreshStart = System.nanoTime();
            fx.index.refreshNow();
            double refreshMs = (System.nanoTime() - refreshStart) / 1e6;
            long total = fx.search.count(LogQuery.all());
            assertThat(total - warmup).isEqualTo((long) CLIENTS * LOGS_PER_CLIENT);

            double searchMs = timeMs(() -> fx.search.search(LogQuery.all().withText("timeout AND inventory")));
            double filterMs = timeMs(() -> fx.search.search(LogQuery.all()
                    .withLevels(Set.of("ERROR")).withServices(Set.of("payment"))));
            double histMs = timeMs(() -> fx.search.histogram(LogQuery.all(), Duration.ofMinutes(1)));
            double countByMs = timeMs(() -> fx.search.countBy(LogField.SERVICE, LogQuery.all()));

            System.out.printf("""

                    ===== LogStream ingestion benchmark =====
                    JVM            : %s, %d CPUs, max heap %d MB
                    clients        : %d parallel gRPC streams x %,d logs
                    logs ingested  : %,d in %.2f s  ->  %,.0f logs/sec
                    refresh        : %.0f ms to make them searchable
                    index size     : %,d docs
                    full-text      : %.1f ms  ("timeout AND inventory", top 50, newest first)
                    filter         : %.1f ms  (level=ERROR, service=payment)
                    histogram      : %.1f ms  (per minute, all docs)
                    countBy        : %.1f ms  (by service, all docs)
                    =========================================
                    %n""",
                    Runtime.version(), Runtime.getRuntime().availableProcessors(),
                    Runtime.getRuntime().maxMemory() / (1024 * 1024),
                    CLIENTS, LOGS_PER_CLIENT, accepted, ingestSec, accepted / ingestSec,
                    refreshMs, total, searchMs, filterMs, histMs, countByMs);
        }
    }

    private static long send(IngestionServerFixture fx, int perClient) throws Exception {
        List<CompletableFuture<IngestResponse>> results = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        long baseTs = Instant.parse("2026-10-04T00:00:00Z").toEpochMilli();
        for (int c = 0; c < CLIENTS; c++) {
            int client = c;
            CompletableFuture<IngestResponse> done = new CompletableFuture<>();
            results.add(done);
            Thread t = Thread.ofPlatform().start(() -> {
                StreamObserver<LogMessage> stream =
                        fx.async().ingestStream(GrpcIngestionIntegrationTest.responseObserver(done));
                for (int i = 0; i < perClient; i++) {
                    int k = i % MESSAGES.length;
                    stream.onNext(LogMessage.newBuilder()
                            .setTimestampMillis(baseTs + (long) i * 50)
                            .setLevel(i % 20 == 0 ? LogLevel.ERROR : i % 7 == 0 ? LogLevel.WARN : LogLevel.INFO)
                            .setService(SERVICES[k])
                            .setHost("pod-" + client + "-" + (i % 8))
                            .setMessage(MESSAGES[k].formatted(i, i % 97))
                            .putAttributes("env", "prod")
                            .build());
                }
                stream.onCompleted();
            });
            threads.add(t);
        }
        for (Thread t : threads) {
            t.join();
        }
        long accepted = 0;
        for (CompletableFuture<IngestResponse> f : results) {
            accepted += f.get(5, TimeUnit.MINUTES).getAccepted();
        }
        return accepted;
    }

    private static double timeMs(Supplier<?> op) {
        for (int i = 0; i < 2; i++) {
            op.get();
        }
        double[] runs = new double[5];
        for (int i = 0; i < runs.length; i++) {
            long s = System.nanoTime();
            op.get();
            runs[i] = (System.nanoTime() - s) / 1e6;
        }
        java.util.Arrays.sort(runs);
        return runs[runs.length / 2];
    }
}
