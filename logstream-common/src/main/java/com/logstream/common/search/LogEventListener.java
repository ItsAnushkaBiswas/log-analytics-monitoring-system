package com.logstream.common.search;

import com.logstream.common.dto.LogRecord;

import java.util.List;

@FunctionalInterface
public interface LogEventListener {
    void onLogsIngested(List<LogRecord> logs);
}
