package com.logstream.searchapi.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logstream.common.dto.LogRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.CloseStatus;

import java.util.List;

import static org.mockito.Mockito.*;

class LogWebSocketHandlerTest {

    private LogWebSocketHandler handler;
    private ObjectMapper objectMapper;
    private WebSocketSession session;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        handler = new LogWebSocketHandler(objectMapper);
        session = mock(WebSocketSession.class);
    }

    @Test
    void shouldSendLogToConnectedSession() throws Exception {
        when(session.isOpen()).thenReturn(true);
        when(objectMapper.writeValueAsString(any(LogRecord.class)))
                .thenReturn("{\"message\":\"test log\"}");

        handler.afterConnectionEstablished(session);
        handler.onLogsIngested(List.of(mock(LogRecord.class)));

        verify(session).sendMessage(
                new TextMessage("{\"message\":\"test log\"}")
        );
    }

    @Test
    void shouldNotSendLogToClosedSession() throws Exception {
        when(session.isOpen()).thenReturn(false);

        handler.afterConnectionEstablished(session);
        handler.onLogsIngested(List.of(mock(LogRecord.class)));

        verify(session, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void shouldRemoveSessionAfterDisconnect() throws Exception {
        when(session.isOpen()).thenReturn(true);
        when(objectMapper.writeValueAsString(any(LogRecord.class)))
                .thenReturn("{\"message\":\"test log\"}");

        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        handler.onLogsIngested(List.of(mock(LogRecord.class)));

        verify(session, never()).sendMessage(any(TextMessage.class));
    }
}
