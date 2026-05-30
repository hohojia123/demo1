package com.example.demo.mapper;

import com.example.demo.model.pojo.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMapper {
    void saveMessage(ChatMessage message);
    List<ChatMessage> findRecentMessages(@Param("limit") int limit);
}
