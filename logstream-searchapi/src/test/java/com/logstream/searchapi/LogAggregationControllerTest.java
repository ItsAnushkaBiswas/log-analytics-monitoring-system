package com.logstream.searchapi;

import com.logstream.searchapi.aggregation.LogAggregationController;
import com.logstream.searchapi.aggregation.LogAggregationService;
import com.logstream.common.search.LogField;
import com.logstream.common.search.LogQuery;
import com.logstream.common.search.LogSearchService;
import com.logstream.common.search.TimeBucket;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LogAggregationControllerTest {

    @Test
    void count_shouldReturnTotalCount() {
        LogSearchService searchService = mock(LogSearchService.class);
        LogAggregationService aggregationService =
                new LogAggregationService(searchService);
        LogAggregationController controller =
                new LogAggregationController(aggregationService);

        when(searchService.count(any(LogQuery.class))).thenReturn(6L);

        long result = (Long) controller.count().get("count");

        assertEquals(6L, result);
        verify(searchService).count(any(LogQuery.class));
    }

    @Test
    void countBy_shouldReturnCountsByLevel() {
        LogSearchService searchService = mock(LogSearchService.class);
        LogAggregationService aggregationService =
                new LogAggregationService(searchService);
        LogAggregationController controller =
                new LogAggregationController(aggregationService);

        when(searchService.countBy(
                eq(LogField.LEVEL), any(LogQuery.class)
        )).thenReturn(Map.of("INFO", 6L));

        Map<String, Object> result = controller.countBy("LEVEL");

        assertEquals(Map.of("counts", Map.of("INFO", 6L)), result);
        verify(searchService).countBy(
                eq(LogField.LEVEL), any(LogQuery.class)
        );
    }

    @Test
    void histogram_shouldReturnTimeBuckets() {
        LogSearchService searchService = mock(LogSearchService.class);
        LogAggregationService aggregationService =
                new LogAggregationService(searchService);
        LogAggregationController controller =
                new LogAggregationController(aggregationService);

        List<TimeBucket> expected = List.of();

        when(searchService.histogram(
                any(LogQuery.class), eq(Duration.ofMinutes(1))
        )).thenReturn(expected);

        Map<String, Object> result = controller.histogram("PT1M");
        assertEquals("PT1M", result.get("interval"));
        assertEquals(expected, result.get("buckets"));
        verify(searchService).histogram(
                any(LogQuery.class), eq(Duration.ofMinutes(1))
        );
    }
}

