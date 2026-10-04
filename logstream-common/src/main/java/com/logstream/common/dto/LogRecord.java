package com.logstream.common.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 1
 * Shared log record used by every module after a log is parsed and stored.
 */
public record LogRecord(
        String id,
        Instant timestamp,
        String level,
        String service,
        String host,
        String message,
        Map<String, String> attributes,
        String traceId) {
    public LogRecord {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        host = host == null ? "" : host;
        traceId = traceId == null ? "" : traceId;
    }
}
