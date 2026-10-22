
package com.logstream.searchapi.aggregation;

import com.logstream.common.search.LogField;
import com.logstream.common.search.LogQuery;
import com.logstream.common.search.TimeBucket;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
import java.util.Set;

@RestController
@RequestMapping("/api/logs/aggregation")
public class LogAggregationController {

    private final LogAggregationService aggregationService;

    public LogAggregationController(LogAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @GetMapping("/count")
    public Map<String, Object> count() {
        LogQuery query = LogQuery.all();

        long count = aggregationService.count(query);
        return Map.of("count", count);
    }

    @GetMapping("/histogram")
    public Map<String, Object> histogram(
            @RequestParam(name = "interval", defaultValue = "PT1M")
            String interval) {

        Duration duration;

        try {
            duration = Duration.parse(interval);
        } catch (java.time.format.DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid interval. Use a duration such as PT1M or PT5M."
            );
        }

        if (duration.isZero() || duration.isNegative()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Interval must be positive."
            );
        }

        LogQuery query = LogQuery.all();
        List<TimeBucket> buckets =
                aggregationService.histogram(query, duration);

        return Map.of(
                "buckets", buckets,
                "interval", interval
        );
    }

    @GetMapping("/count-by")
    public Map<String, Object> countBy(
            @RequestParam(name = "field", defaultValue = "LEVEL")
            String field) {

        LogQuery query = LogQuery.all();

        LogField logField = LogField.valueOf(field.toUpperCase());

        Map<String, Long> counts = aggregationService.countBy(
                logField, query
        );

        return Map.of("counts", counts);
    }
}
    public long count(
            @RequestParam(name = "text", required = false) String text,
            @RequestParam(name = "levels", required = false) Set<String> levels,
            @RequestParam(name = "services", required = false) Set<String> services,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to) {

        LogQuery query = createQuery(text, levels, services, from, to);

        return aggregationService.count(query);
    }

    @GetMapping("/histogram")
    public List<TimeBucket> histogram(
            @RequestParam(name = "text", required = false) String text,
            @RequestParam(name = "levels", required = false) Set<String> levels,
            @RequestParam(name = "services", required = false) Set<String> services,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to,
            @RequestParam(name = "interval", defaultValue = "PT1M") String interval) {

        LogQuery query = createQuery(text, levels, services, from, to);

        Duration duration = Duration.parse(interval);

        return aggregationService.histogram(query, duration);
    }

    @GetMapping("/count-by")
    public Map<String, Long> countBy(
            @RequestParam(name = "field") LogField field,
            @RequestParam(name = "text", required = false) String text,
            @RequestParam(name = "levels", required = false) Set<String> levels,
            @RequestParam(name = "services", required = false) Set<String> services,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to) {

        LogQuery query = createQuery(text, levels, services, from, to);

        return aggregationService.countBy(field, query);
    }

    private LogQuery createQuery(
            String text,
            Set<String> levels,
            Set<String> services,
            Instant from,
            Instant to) {

        return new LogQuery(
                text,
                levels,
                services,
                from,
                to,
                0,
                LogQuery.DEFAULT_LIMIT
        );
    }
}
