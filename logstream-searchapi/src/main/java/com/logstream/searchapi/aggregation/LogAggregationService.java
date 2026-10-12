package com.logstream.searchapi.aggregation;

import com.logstream.common.search.LogField;
import com.logstream.common.search.LogQuery;
import com.logstream.common.search.TimeBucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class LogAggregationService {

    private final com.logstream.common.search.LogSearchService commonSearchService;

    public LogAggregationService(
            com.logstream.common.search.LogSearchService commonSearchService) {
        this.commonSearchService = commonSearchService;
    }

    public long count(LogQuery query) {
        return commonSearchService.count(query);
    }

    public List<TimeBucket> histogram(LogQuery query, Duration interval) {
        return commonSearchService.histogram(query, interval);
    }

    public Map<String, Long> countBy(LogField field, LogQuery query) {
        return commonSearchService.countBy(field, query);
    }
}
