package com.logstream.common.search;

import java.time.Instant;
import java.util.Map;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 3
 * One time slot of the log volume histogram used by the ECharts dashboard.
 */
public record TimeBucket(Instant start, long count, Map<String, Long> byLevel) {
    public TimeBucket {
        byLevel = Map.copyOf(byLevel);
    }
}
