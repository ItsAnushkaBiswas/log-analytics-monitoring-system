package com.logstream.common.search;

import com.logstream.common.dto.LogRecord;

import java.util.List;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 2
 * One page of search results returned to the Search API.
 */
public record SearchResult(long total, List<LogRecord> logs, long tookMs) {
    public SearchResult {
        logs = List.copyOf(logs);
    }
}
