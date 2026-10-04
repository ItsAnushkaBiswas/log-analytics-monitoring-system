package com.logstream.ingestion.grpc;

import com.logstream.common.dto.LogRecord;
import com.logstream.common.proto.LogIngestionServiceGrpc;
import com.logstream.common.search.LogEventListener;
import com.logstream.ingestion.TestIndexes;
import com.logstream.ingestion.config.IngestionProperties;
import com.logstream.ingestion.lucene.LuceneIndexManager;
import com.logstream.ingestion.lucene.LuceneLogSearchService;
import com.logstream.ingestion.parser.LogParser;
import com.logstream.ingestion.service.IngestionService;
import com.logstream.ingestion.service.IngestionStats;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 4
 * Test helper that starts the full ingestion stack on a random port.
 */
final class IngestionServerFixture implements AutoCloseable {
    final LuceneIndexManager index;
    final LuceneLogSearchService search;
    final IngestionStats stats = new IngestionStats();
    final List<LogRecord> pushed = new CopyOnWriteArrayList<>();
    final GrpcServerLifecycle server;
    final ManagedChannel channel;

    IngestionServerFixture(Path dir) {
        IngestionProperties props = TestIndexes.properties(dir);
        index = new LuceneIndexManager(props);
        search = new LuceneLogSearchService(index);
        LogEventListener listener = logs -> {
            if (pushed.size() < 10_000) {
                pushed.addAll(logs);
            }
        };
        IngestionService ingestion = new IngestionService(new LogParser(), index, stats, List.of(listener));
        server = new GrpcServerLifecycle(props, new LogIngestionGrpcService(ingestion, props));
        server.start();
        channel = NettyChannelBuilder.forAddress("localhost", server.port()).usePlaintext().build();
    }

    LogIngestionServiceGrpc.LogIngestionServiceBlockingStub blocking() {
        return LogIngestionServiceGrpc.newBlockingStub(channel);
    }

    LogIngestionServiceGrpc.LogIngestionServiceStub async() {
        return LogIngestionServiceGrpc.newStub(channel);
    }

    @Override
    public void close() throws Exception {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        server.stop();
        index.close();
    }
}
