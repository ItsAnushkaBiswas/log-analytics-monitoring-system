package com.logstream.searchapi.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logstream.common.dto.LogRecord;
import com.logstream.common.search.LogEventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class LogWebSocketHandler extends TextWebSocketHandler
        implements LogEventListener {

    private final Set<WebSocketSession> sessions =
            new CopyOnWriteArraySet<>();

    private final ObjectMapper objectMapper;

    public LogWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);

        System.out.println(
                "WebSocket client connected: " + session.getId()
        );
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws IOException {

        session.sendMessage(
                new TextMessage("Live Tail WebSocket is working")
        );
    }

    @Override
    public void onLogsIngested(List<LogRecord> logs) {

        for (LogRecord log : logs) {

            try {
                String json = objectMapper.writeValueAsString(log);

                for (WebSocketSession session : sessions) {

                    if (session.isOpen()) {
                        session.sendMessage(
                                new TextMessage(json)
                        );
                    }
                }

            } catch (IOException e) {
                System.err.println(
                        "Failed to send log over WebSocket: "
                                + e.getMessage()
                );
            }
        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            org.springframework.web.socket.CloseStatus status) {

        sessions.remove(session);

        System.out.println(
                "WebSocket client disconnected: " + session.getId()
        );
    }
}