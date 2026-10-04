package com.logstream.common.dto;

import java.time.Instant;
import java.util.Map;

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
