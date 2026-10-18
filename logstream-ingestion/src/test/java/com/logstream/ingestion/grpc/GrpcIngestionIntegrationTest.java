package com.logstream.ingestion.grpc;

import com.logstream.common.proto.IngestResponse;
import com.logstream.common.proto.LogBatch;
import com.logstream.common.proto.LogLevel;
import com.logstream.common.proto.LogMessage;
import com.logstream.common.search.LogQuery;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 4
 * End-to-end tests over a real gRPC connection into the Lucene index.
 */
class GrpcIngestionIntegrationTest {
    @TempDir
    Path dir;

    private IngestionServerFixture fx;

    @BeforeEach
    void setUp() {
        fx = new IngestionServerFixture(dir);
    }

    @AfterEach
    void tearDown() throws Exception {
        fx.close();
    }

    @Test
    void singleLog() throws Exception {
        IngestResponse r = fx.blocking().ingest(LogMessage.newBuilder()
                .setLevel(LogLevel.ERROR).setService("payment").setMessage("card declined").build());

        assertThat(r.getAccepted()).isEqualTo(1);
        assertThat(r.getRejected()).isZero();
        fx.index.refreshNow();
        assertThat(fx.search.search(LogQuery.all().withText("declined")).logs())
                .singleElement().satisfies(log -> assertThat(log.service()).isEqualTo("payment"));
    }

    @Test
    void batchWithAnInvalidLog() throws Exception {
        IngestResponse r = fx.blocking().ingestBatch(LogBatch.newBuilder()
                .addLogs(LogMessage.newBuilder().setMessage("ok one"))
                .addLogs(LogMessage.newBuilder().setService("no message"))
                .addLogs(LogMessage.newBuilder().setRaw("2026-10-04T10:05:00Z WARN [auth] slow login"))
                .build());

        assertThat(r.getAccepted()).isEqualTo(2);
        assertThat(r.getRejected()).isEqualTo(1);
        assertThat(r.getErrorsList()).containsExactly("#1: message is required (set 'message' or 'raw')");
        fx.index.refreshNow();
        assertThat(fx.search.count(LogQuery.all().withLevels(Set.of("WARN")).withServices(Set.of("auth")))).isEqualTo(1);
    }

    @Test
    void streamLargerThanOneBatch() throws Exception {
        int n = 2_500;
        CompletableFuture<IngestResponse> done = new CompletableFuture<>();
        StreamObserver<LogMessage> stream = fx.async().ingestStream(responseObserver(done));
        for (int i = 0; i < n; i++) {
            stream.onNext(LogMessage.newBuilder()
                    .setTimestampMillis(1_780_000_000_000L + i)
                    .setLevel(i % 10 == 0 ? LogLevel.ERROR : LogLevel.INFO)
                    .setService("svc-" + (i % 3))
                    .setMessage(i == 1_234 ? "" : "request " + i + " handled")
                    .build());
        }
        stream.onCompleted();

        IngestResponse r = done.get(30, TimeUnit.SECONDS);
        assertThat(r.getAccepted()).isEqualTo(n - 1);
        assertThat(r.getErrorsList()).containsExactly("#1234: message is required (set 'message' or 'raw')");
        fx.index.refreshNow();
        assertThat(fx.search.count(LogQuery.all())).isEqualTo(n - 1);
        assertThat(fx.search.count(LogQuery.all().withLevels(Set.of("ERROR")))).isEqualTo(250);
        assertThat(fx.stats.accepted()).isEqualTo(n - 1);
    }

    @Test
    void listenersReceiveNewLogsImmediately() {
        fx.blocking().ingest(LogMessage.newBuilder().setMessage("live tail me").build());

        assertThat(fx.pushed).singleElement()
                .satisfies(log -> assertThat(log.message()).isEqualTo("live tail me"));
    }

    static StreamObserver<IngestResponse> responseObserver(CompletableFuture<IngestResponse> done) {
        return new StreamObserver<>() {
            @Override
            public void onNext(IngestResponse value) {
                done.complete(value);
            }

            @Override
            public void onError(Throwable t) {
                done.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
            }
        };
    }
}
