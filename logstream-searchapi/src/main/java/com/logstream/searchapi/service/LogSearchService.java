package com.logstream.searchapi.service;

import com.logstream.searchapi.dto.LogSearchResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class LogSearchService {

    public LogSearchResponse searchLogs() {
        return new LogSearchResponse(
                Collections.emptyList(),
                0,
                0,
                10
        );
    }
}
