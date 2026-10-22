
package com.logstream.searchapi.controller;

import com.logstream.common.search.LogQuery;
import com.logstream.searchapi.dto.LogSearchResponse;
import com.logstream.searchapi.service.LogSearchService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Set;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
@RestController
@RequestMapping("/api/logs")
public class LogSearchController {

    private final LogSearchService logSearchService;

    public LogSearchController(LogSearchService logSearchService) {
        this.logSearchService = logSearchService;
    }


    @GetMapping("/search")
    public LogSearchResponse searchLogs(
            @RequestParam(name = "text", required = false) String text,
            @RequestParam(name = "levels", required = false) Set<String> levels,
            @RequestParam(name = "services", required = false) Set<String> services,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Page must be 0 or greater");
        }

        if (size < 1 || size > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Size must be between 1 and 500");
        }

        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "'from' must not be after 'to'");
        }

        int offset = Math.multiplyExact(page, size);

        LogQuery query = new LogQuery(
                text, levels, services, from, to, offset, size
        );

        return logSearchService.searchLogs(query);
    }

}
