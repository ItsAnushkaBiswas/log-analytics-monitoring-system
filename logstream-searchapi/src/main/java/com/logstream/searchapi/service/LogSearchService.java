package com.logstream.searchapi.service;

import com.logstream.common.dto.LogRecord;
import com.logstream.common.search.LogQuery;
import com.logstream.common.search.SearchResult;
import com.logstream.searchapi.dto.LogResponse;
import com.logstream.searchapi.dto.LogSearchResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogSearchService {

    private final com.logstream.common.search.LogSearchService commonSearchService;

    public LogSearchService(
            com.logstream.common.search.LogSearchService commonSearchService) {
        this.commonSearchService = commonSearchService;
    }

    public LogSearchResponse searchLogs(LogQuery query) {

        SearchResult searchResult = commonSearchService.search(query);

        List<LogResponse> results = searchResult.logs()
                .stream()
                .map(this::toLogResponse)
                .toList();

        int page = query.limit() > 0
                ? query.offset() / query.limit()
                : 0;

        return new LogSearchResponse(
                results,
                searchResult.total(),
                page,
                query.limit(),
                searchResult.tookMs()
        );
    }

    private LogResponse toLogResponse(LogRecord log) {

        return new LogResponse(
                log.id(),
                log.timestamp(),
                log.level(),
                log.service(),
                log.host(),
                log.message(),
                log.attributes(),
                log.traceId()
        );
    }
}