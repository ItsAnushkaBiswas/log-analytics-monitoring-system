package com.logstream.common.search;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 2
 * Search interface used by Member 2 (Search API) and Member 4 (Alerting). Implemented by LuceneLogSearchService.
 */
public interface LogSearchService {
    SearchResult search(LogQuery query);

    long count(LogQuery query);

    List<TimeBucket> histogram(LogQuery query, Duration interval);

    Map<String, Long> countBy(LogField field, LogQuery query);
}
