package com.logstream.common.search;

import com.logstream.common.dto.LogRecord;

import java.util.List;

public record SearchResult(long total, List<LogRecord> logs, long tookMs) {
    public SearchResult {
        logs = List.copyOf(logs);
    }
}
