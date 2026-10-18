package com.logstream.searchapi;

import com.logstream.common.proto.IngestResponse;
import com.logstream.common.proto.LogBatch;

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
                    .setService("dashboard-test")
                    .setHost("localhost")
                    .setMessage("Test error from LogStream dashboard")
                    .setRaw("ERROR dashboard-test: Test error from LogStream dashboard")
                    .build();

            LogBatch batch = LogBatch.newBuilder()
                    .addLogs(log)
                    .build();

            IngestResponse response = stub.ingestBatch(batch);

            System.out.println("Accepted: " + response.getAccepted());
            System.out.println("Rejected: " + response.getRejected());
            System.out.println("Errors: " + response.getErrorsList());

        } finally {
            channel.shutdown();
        }
    }
}
