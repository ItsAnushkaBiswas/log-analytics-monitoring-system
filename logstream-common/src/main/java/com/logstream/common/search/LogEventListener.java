package com.logstream.common.search;

import com.logstream.common.dto.LogRecord;

import java.util.List;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 2
 * Hook that pushes newly ingested logs to the Live Tail (Member 2).
 */
@FunctionalInterface
public interface LogEventListener {
    void onLogsIngested(List<LogRecord> logs);
}
