
package com.logstream.searchapi.aggregation;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/logs/aggregation")
public class LogAggregationController {

    private final LogAggregationService aggregationService;

    public LogAggregationController(LogAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @GetMapping("/count")
    public Map<String, Object> count() {
        return aggregationService.count();
    }

    @GetMapping("/histogram")
    public Map<String, Object> histogram(
            @RequestParam(name = "interval", defaultValue = "PT1M")
            String interval) {
        return aggregationService.histogram(interval);
    }

    @GetMapping("/count-by")
    public Map<String, Object> countBy(
            @RequestParam(name = "field", defaultValue = "LEVEL")
            String field) {
        return aggregationService.countBy(field);
    }
}