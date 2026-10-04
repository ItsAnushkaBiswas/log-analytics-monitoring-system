package com.logstream.ingestion.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 1
 * Registers the ingestion settings with Spring.
 */
@Configuration
@EnableConfigurationProperties(IngestionProperties.class)
public class IngestionConfig {
}
