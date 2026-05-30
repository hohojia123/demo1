package com.example.demo.service.impl;

import com.example.demo.mapper.ChatMapper;
import com.example.demo.model.pojo.ChatMessage;
import com.example.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatMapper chatMapper;

    @Override
    public void saveMessage(ChatMessage message) {
        chatMapper.saveMessage(message);
    }

    @Override
    public List<ChatMessage> getRecentMessages(int limit) {
        return chatMapper.findRecentMessages(limit);
    }
}
