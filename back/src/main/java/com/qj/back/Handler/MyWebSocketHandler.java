package com.qj.back.Handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class MyWebSocketHandler extends TextWebSocketHandler {
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket connected, sessionId={}, activeSessions={}", session.getId(), sessions.size());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.debug("WebSocket message received, sessionId={}, payload={}", session.getId(), message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WebSocket closed, sessionId={}, status={}, activeSessions={}", session.getId(), status, sessions.size());
    }

    public synchronized void sendToAllClients(String message) {
        ArrayList<String> failedSessions = new ArrayList<>();
        TextMessage textMessage = new TextMessage(message);
        log.debug("Broadcasting WebSocket message to {} sessions", sessions.size());

        sessions.forEach((sessionId, session) -> {
            try {
                if (session.isOpen()) {
                    synchronized (session) {
                        session.sendMessage(textMessage);
                    }
                } else {
                    failedSessions.add(sessionId);
                }
            } catch (Exception e) {
                log.warn("Failed to send WebSocket message, sessionId={}, error={}", sessionId, e.getMessage());
                failedSessions.add(sessionId);
            }
        });

        failedSessions.forEach(sessions::remove);
        if (!failedSessions.isEmpty()) {
            log.info("Removed {} invalid WebSocket sessions, activeSessions={}", failedSessions.size(), sessions.size());
        }
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }
}
