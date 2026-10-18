package com.logstream.searchapi.aggregation;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.logstream.common.search.LogQuery;
import com.logstream.common.search.SearchResult;

@Service
public class LogAggregationService {

    private final com.logstream.common.search.LogSearchService commonSearchService;

    public LogAggregationService(
            com.logstream.common.search.LogSearchService commonSearchService) {
        this.commonSearchService = commonSearchService;
    }
private SearchResult getSearchResult() {
    LogQuery query = new LogQuery(
        "*:*",
        null,
        null,
        null,
        null,
        0,
        1000
    );

    return commonSearchService.search(query);
}
    public Map<String, Object> count() {
        SearchResult result = getSearchResult();

        return Map.of(
                "count", result.total(),
                "status", "ok"
        );
    }

    public Map<String, Object> histogram(String interval) {
        SearchResult result = getSearchResult();

        List<Map<String, Object>> data = result.logs().stream()
                .filter(log -> log.timestamp() != null)
                .collect(Collectors.groupingBy(
                        log -> log.timestamp()
                                .truncatedTo(ChronoUnit.HOURS)
                                .toString(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> Map.<String, Object>of(
                        "time", entry.getKey(),
                        "count", entry.getValue()
                ))
                .toList();

        return Map.of(
                "interval", interval,
                "data", data
        );
    }

    public Map<String, Object> countBy(String field) {
        SearchResult result = getSearchResult();
        boolean byService = "SERVICE".equalsIgnoreCase(field);

        List<Map<String, Object>> data = result.logs().stream()
                .collect(Collectors.groupingBy(
                        log -> {
                            String value = byService
                                    ? log.service()
                                    : log.level();
                            return Objects.toString(value, "UNKNOWN");
                        },
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> Map.<String, Object>of(
                        "name", entry.getKey(),
                        "count", entry.getValue()
                ))
                .toList();

        return Map.of(
                "field", byService ? "SERVICE" : "LEVEL",
                "data", data
        );
    }
}
