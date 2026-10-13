
package com.logstream.searchapi;

import com.logstream.common.search.LogQuery;
import com.logstream.searchapi.controller.LogSearchController;
import com.logstream.searchapi.dto.LogSearchResponse;
import com.logstream.searchapi.service.LogSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogSearchControllerTest {

    @Test
    void searchLogs_shouldReturnSearchResponse() {
        LogSearchService service = mock(LogSearchService.class);

        LogSearchResponse expectedResponse =
                new LogSearchResponse(
                        java.util.List.of(),
                        0,
                        0,
                        50,
                        10
                );

        when(service.searchLogs(any(LogQuery.class)))
                .thenReturn(expectedResponse);

        LogSearchController controller =
                new LogSearchController(service);

        LogSearchResponse response = controller.searchLogs(
                null, Set.of(), Set.of(),
                null, null, 0, 50
        );

        assertNotNull(response);
        assertEquals(0, response.getTotal());
        assertEquals(0, response.getPage());
        assertEquals(50, response.getSize());

        verify(service, times(1))
                .searchLogs(any(LogQuery.class));
    }

    @Test
    void searchLogs_shouldRejectNegativePage() {
        LogSearchController controller =
                new LogSearchController(mock(LogSearchService.class));

        assertThrows(ResponseStatusException.class, () ->
                controller.searchLogs(
                        null, null, null,
                        null, null, -1, 50
                )
        );
    }

    @Test
    void searchLogs_shouldRejectSizeAbove500() {
        LogSearchController controller =
                new LogSearchController(mock(LogSearchService.class));

        assertThrows(ResponseStatusException.class, () ->
                controller.searchLogs(
                        null, null, null,
                        null, null, 0, 501
                )
        );
    }

    @Test
    void searchLogs_shouldRejectFromAfterTo() {
        LogSearchController controller =
                new LogSearchController(mock(LogSearchService.class));

        Instant from = Instant.parse("2026-10-13T12:00:00Z");
        Instant to = Instant.parse("2026-10-13T10:00:00Z");

        assertThrows(ResponseStatusException.class, () ->
                controller.searchLogs(
                        null, null, null,
                        from, to, 0, 50
                )
        );
    }
}
