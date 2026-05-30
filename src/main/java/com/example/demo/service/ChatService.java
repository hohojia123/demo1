package com.example.demo.service;

import com.example.demo.model.pojo.ChatMessage;

import java.util.List;

public interface ChatService {
    void saveMessage(ChatMessage message);
    List<ChatMessage> getRecentMessages(int limit);
}
