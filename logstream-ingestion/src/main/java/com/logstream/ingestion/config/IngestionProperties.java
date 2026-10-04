package com.logstream.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("logstream.ingestion")
public record IngestionProperties(
        @DefaultValue("9090") int grpcPort,
        @DefaultValue("index-data") String indexPath,
        @DefaultValue("1000") int maxBatchSize,
        @DefaultValue("64") double ramBufferMb,
        @DefaultValue("1s") Duration refreshInterval,
        @DefaultValue("5s") Duration commitInterval,
        @DefaultValue("16") int maxInboundMessageMb) {
}
