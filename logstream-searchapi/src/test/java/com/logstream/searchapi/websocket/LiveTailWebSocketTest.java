package com.logstream.searchapi.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.Mockito.*;

class LiveTailWebSocketTest {

    @Test
    void shouldRespondWhenWebSocketMessageIsReceived() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        LogWebSocketHandler handler = new LogWebSocketHandler(objectMapper);

        WebSocketSession session = mock(WebSocketSession.class);

        when(session.getId()).thenReturn("test-session");
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);

        handler.handleMessage(
                session,
                new TextMessage("test")
        );

        verify(session).sendMessage(
                new TextMessage("Live Tail WebSocket is working")
        );
    }
}