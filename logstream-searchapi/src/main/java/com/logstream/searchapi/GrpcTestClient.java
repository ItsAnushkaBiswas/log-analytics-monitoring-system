package com.logstream.searchapi;

import com.logstream.common.proto.IngestResponse;
import com.logstream.common.proto.LogIngestionServiceGrpc;
import com.logstream.common.proto.LogLevel;
import com.logstream.common.proto.LogMessage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcTestClient {

    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        try {
            LogIngestionServiceGrpc.LogIngestionServiceBlockingStub stub =
                    LogIngestionServiceGrpc.newBlockingStub(channel);

            LogMessage log = LogMessage.newBuilder()
                    .setTimestampMillis(System.currentTimeMillis())
                    .setLevel(LogLevel.ERROR)
                    .setService("test-service")
                    .setHost("localhost")
                    .setMessage("Alert testing error log 2026")
                    .setLevel(LogLevel.INFO)
                    .setService("test-service")
                    .setHost("localhost")
                    .setMessage("Member2 integration test 1014")
                    .putAttributes("environment", "test")
                    .setTraceId("test-trace-001")
                    .setRaw("INFO test-service Live Tail test log")
                    .build();

            IngestResponse response = stub.ingest(log);

            System.out.println("Accepted: " + response.getAccepted());
            System.out.println("Rejected: " + response.getRejected());

            if (response.getErrorsCount() > 0) {
                System.out.println("Errors: " + response.getErrorsList());
            }

        } finally {
            channel.shutdown();
        }
    }
}
