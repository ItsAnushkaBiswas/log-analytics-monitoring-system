package com.logstream.ingestion.grpc;

import com.logstream.ingestion.config.IngestionProperties;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 1
 * Starts and stops the gRPC ingestion server on port 9090.
 */
@Component
public class GrpcServerLifecycle implements SmartLifecycle {
    private static final Logger log = LoggerFactory.getLogger(GrpcServerLifecycle.class);

    private final IngestionProperties props;
    private final BindableService ingestionService;
    private volatile Server server;

    public GrpcServerLifecycle(IngestionProperties props, LogIngestionGrpcService ingestionService) {
        this.props = props;
        this.ingestionService = ingestionService;
    }

    @Override
    public void start() {
        try {
            server = NettyServerBuilder.forPort(props.grpcPort())
                    .addService(ingestionService)
                    .addService(ProtoReflectionServiceV1.newInstance())
                    .maxInboundMessageSize(props.maxInboundMessageMb() * 1024 * 1024)
                    .build()
                    .start();
            log.info("gRPC ingestion server listening on port {}", server.getPort());

            Server running = server;
            Thread keepAlive = new Thread(() -> {
                try {
                    running.awaitTermination();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "grpc-await-termination");
            keepAlive.setDaemon(false);
            keepAlive.start();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot start gRPC server on port " + props.grpcPort(), e);
        }
    }

    @Override
    public void stop() {
        Server s = server;
        if (s == null) {
            return;
        }
        s.shutdown();
        try {
            if (!s.awaitTermination(10, TimeUnit.SECONDS)) {
                s.shutdownNow();
            }
        } catch (InterruptedException e) {
            s.shutdownNow();
            Thread.currentThread().interrupt();
        }
        server = null;
        log.info("gRPC ingestion server stopped");
    }

    @Override
    public boolean isRunning() {
        return server != null;
    }

    public int port() {
        return server.getPort();
    }
}
