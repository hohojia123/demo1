package com.example.demo.controller;

import com.example.demo.model.entity.Result;
import com.example.demo.model.pojo.ChatMessage;
import com.example.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/history")
    public Result getHistory(@RequestParam(defaultValue = "50") int limit) {
        if (limit > 200) limit = 200;
        if (limit < 1) limit = 50;
        List<ChatMessage> messages = chatService.getRecentMessages(limit);
        return Result.success("查询成功", messages);
    }
}
