package com.logstream.common.search;

import java.time.Instant;
import java.util.Map;

public record TimeBucket(Instant start, long count, Map<String, Long> byLevel) {
    public TimeBucket {
        byLevel = Map.copyOf(byLevel);
    }
}
