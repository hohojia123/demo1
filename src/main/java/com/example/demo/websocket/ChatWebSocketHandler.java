package com.example.demo.websocket;

import com.example.demo.configuration.JwtConfi;
import com.example.demo.mapper.userMapper;
import com.example.demo.model.pojo.ChatMessage;
import com.example.demo.model.pojo.User;
import com.example.demo.service.ChatService;
import com.example.demo.uitl.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private JwtConfi jwtConfig;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private userMapper userMapper;
    @Autowired
    private ChatService chatService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session);
        if (token == null || !validateToken(token)) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        sessions.add(session);
        log.info("WebSocket连接建立: {}, 当前在线: {}", session.getId(), sessions.size());

        // 发送最近消息历史
        List<ChatMessage> recentMessages = chatService.getRecentMessages(50);
        if (!recentMessages.isEmpty()) {
            Map<String, Object> historyMsg = new HashMap<>();
            historyMsg.put("type", "history");
            historyMsg.put("messages", recentMessages);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(historyMsg)));
        }

        // 广播在线人数
        broadcastOnlineCount();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String, Object> msgMap = objectMapper.readValue(payload, Map.class);
        String type = (String) msgMap.get("type");

        if ("chat".equals(type)) {
            String content = (String) msgMap.get("content");
            if (content == null || content.trim().isEmpty()) return;
            if (content.length() > 500) content = content.substring(0, 500);

            // 获取用户信息
            Integer userId = (Integer) session.getAttributes().get("userId");
            String userName = (String) session.getAttributes().get("userName");
            String userAvatar = (String) session.getAttributes().get("userAvatar");

            ChatMessage chatMsg = new ChatMessage();
            chatMsg.setUserId(userId);
            chatMsg.setUserName(userName);
            chatMsg.setUserAvatar(userAvatar);
            chatMsg.setContent(content.trim());
            chatMsg.setCreatedAt(LocalDateTime.now());

            // 保存到数据库
            chatService.saveMessage(chatMsg);

            // 广播给所有在线用户
            Map<String, Object> broadcastMsg = new HashMap<>();
            broadcastMsg.put("type", "chat");
            broadcastMsg.put("id", chatMsg.getId());
            broadcastMsg.put("userId", userId);
            broadcastMsg.put("userName", userName);
            broadcastMsg.put("userAvatar", userAvatar);
            broadcastMsg.put("content", content.trim());
            broadcastMsg.put("createdAt", chatMsg.getCreatedAt().toString());

            String broadcastPayload = objectMapper.writeValueAsString(broadcastMsg);
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(broadcastPayload));
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket连接关闭: {}, 当前在线: {}", session.getId(), sessions.size());
        broadcastOnlineCount();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误: {}", session.getId(), exception);
        sessions.remove(session);
        broadcastOnlineCount();
    }

    private String extractToken(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String query = uri.getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && "token".equals(pair[0])) {
                return pair[1];
            }
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            var claims = jwtUtil.parseJwt(token);
            Integer userId = (Integer) claims.get("userId");
            String key = jwtConfig.REDIS_TOKEN_KEY_PREFIX + userId;
            String redisToken = redisTemplate.opsForValue().get(key);
            if (redisToken == null || !token.equals(redisToken)) return false;

            User user = userMapper.selectById(userId);
            if (user == null || user.getState() == 0) return false;

            // 将用户信息存入session attributes
            Map<String, Object> attrs = new HashMap<>();
            attrs.put("userId", userId);
            attrs.put("userName", user.getName());
            attrs.put("userAvatar", user.getAvatar() != null ? user.getAvatar() : "");
            session.getAttributes().putAll(attrs);
            return true;
        } catch (Exception e) {
            log.error("WebSocket token验证失败", e);
            return false;
        }
    }

    private void broadcastOnlineCount() {
        long count = sessions.stream().filter(WebSocketSession::isOpen).count();
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "online");
        msg.put("count", count);
        try {
            String payload = objectMapper.writeValueAsString(msg);
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(payload));
                }
            }
        } catch (Exception e) {
            log.error("广播在线人数失败", e);
        }
    }
}
