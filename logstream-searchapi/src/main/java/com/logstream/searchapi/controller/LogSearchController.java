package com.logstream.searchapi.controller;

import com.logstream.searchapi.dto.LogSearchResponse;
import com.logstream.searchapi.service.LogSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
public class LogSearchController {

    private final LogSearchService logSearchService;

    public LogSearchController(LogSearchService logSearchService) {
        this.logSearchService = logSearchService;
    }

    @GetMapping("/search")
    public LogSearchResponse searchLogs() {
        return logSearchService.searchLogs();
    }
}
