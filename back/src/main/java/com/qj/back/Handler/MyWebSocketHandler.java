package com.qj.back.Handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("WebSocket连接已建立，sessionId=" + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("接收到消息: " + message.getPayload());

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("WebSocket连接已关闭，sessionId=" + session.getId());
    }

    public synchronized void sendToAllClients(String message) {
        ArrayList<String> failedSessions = new ArrayList<>();
        System.out.println("准备向 " + sessions.size() + " 个客户端发送消息: " + message);
        TextMessage textMessage = new TextMessage(message);

        sessions.forEach((sessionId, session) -> {
            try {
                if (session.isOpen()) {
                    synchronized (session) {
                        session.sendMessage(textMessage);
                        System.out.println("成功发送消息到 sessionId=" + sessionId);
                    }
                } else {
                    System.out.println("会话已关闭，将移除 sessionId=" + sessionId);
                    failedSessions.add(sessionId);
                }
            } catch (Exception e) {
                System.err.println("发送消息到 sessionId=" + sessionId + " 失败: " + e.getMessage());
                failedSessions.add(sessionId);
                e.printStackTrace();
            }
        });

        // 移除失败的会话
        failedSessions.forEach(sessions::remove);
    }

}
