package com.logstream.ingestion.grpc;

import com.logstream.common.proto.IngestResponse;
import com.logstream.common.proto.LogBatch;
import com.logstream.common.proto.LogIngestionServiceGrpc;
import com.logstream.common.proto.LogMessage;
import com.logstream.ingestion.config.IngestionProperties;
import com.logstream.ingestion.service.IngestionService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 1
 * gRPC endpoint that receives logs as single messages, batches or streams.
 */
@Component
public class LogIngestionGrpcService extends LogIngestionServiceGrpc.LogIngestionServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(LogIngestionGrpcService.class);

    private final IngestionService ingestion;
    private final int maxBatchSize;

    public LogIngestionGrpcService(IngestionService ingestion, IngestionProperties props) {
        this.ingestion = ingestion;
        this.maxBatchSize = props.maxBatchSize();
    }

    @Override
    public void ingest(LogMessage request, StreamObserver<IngestResponse> responseObserver) {
        respond(responseObserver, () -> ingestion.ingest(List.of(request), 0));
    }

    @Override
    public void ingestBatch(LogBatch request, StreamObserver<IngestResponse> responseObserver) {
        respond(responseObserver, () -> ingestion.ingest(request.getLogsList(), 0));
    }

    // Week 4: streaming ingestion buffers logs and indexes them in batches for throughput
    @Override
    public StreamObserver<LogMessage> ingestStream(StreamObserver<IngestResponse> responseObserver) {
        return new StreamObserver<>() {
            private final List<LogMessage> buffer = new ArrayList<>(maxBatchSize);
            private final IngestResponse.Builder total = IngestResponse.newBuilder();
            private long position;
            private boolean failed;

            @Override
            public void onNext(LogMessage value) {
                if (failed) {
                    return;
                }
                buffer.add(value);
                if (buffer.size() >= maxBatchSize) {
                    flush();
                }
            }

            @Override
            public void onError(Throwable t) {
                if (!buffer.isEmpty()) {
                    flush();
                }
                log.debug("Ingest stream ended by client error after {} logs: {}", position, t.toString());
            }

            @Override
            public void onCompleted() {
                if (failed) {
                    return;
                }
                if (!buffer.isEmpty()) {
                    flush();
                }
                if (!failed) {
                    responseObserver.onNext(total.build());
                    responseObserver.onCompleted();
                }
            }

            private void flush() {
                try {
                    IngestResponse r = ingestion.ingest(buffer, position);
                    total.setAccepted(total.getAccepted() + r.getAccepted())
                            .setRejected(total.getRejected() + r.getRejected());
                    for (String err : r.getErrorsList()) {
                        if (total.getErrorsCount() < IngestionService.MAX_REPORTED_ERRORS) {
                            total.addErrors(err);
                        }
                    }
                } catch (RuntimeException e) {
                    failed = true;
                    log.error("Ingest stream failed", e);
                    responseObserver.onError(Status.INTERNAL
                            .withDescription("indexing failed: " + e.getMessage()).asRuntimeException());
                } finally {
                    position += buffer.size();
                    buffer.clear();
                }
            }
        };
    }

    private static void respond(StreamObserver<IngestResponse> observer, Supplier<IngestResponse> call) {
        try {
            observer.onNext(call.get());
            observer.onCompleted();
        } catch (RuntimeException e) {
            log.error("Ingest call failed", e);
            observer.onError(Status.INTERNAL.withDescription("indexing failed: " + e.getMessage()).asRuntimeException());
        }
    }
}
