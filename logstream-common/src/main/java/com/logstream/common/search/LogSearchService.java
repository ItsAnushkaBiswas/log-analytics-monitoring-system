package com.logstream.common.search;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public interface LogSearchService {
    SearchResult search(LogQuery query);

    long count(LogQuery query);

    List<TimeBucket> histogram(LogQuery query, Duration interval);

    Map<String, Long> countBy(LogField field, LogQuery query);
}
