package com.logstream.searchapi;

import com.logstream.common.search.LogQuery;
import com.logstream.searchapi.dto.LogSearchResponse;
import com.logstream.searchapi.service.LogSearchService;
import com.logstream.searchapi.controller.LogSearchController;
import org.junit.jupiter.api.Test;

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

        LogSearchResponse response =
                controller.searchLogs(
                        null,
                        Set.of(),
                        Set.of(),
                        null,
                        null,
                        0,
                        50
                );

        assertNotNull(response);
        assertEquals(0, response.getTotal());
        assertEquals(0, response.getPage());
        assertEquals(50, response.getSize());

        verify(service, times(1))
                .searchLogs(any(LogQuery.class));
    }
}
