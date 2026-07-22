package com.example.demo.controller;

import com.example.demo.service.AiChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class AiChatController {

    private AiChatService chatClient;

    @Autowired
    public AiChatController(AiChatService chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/private")
    public String Prichat(@RequestParam("message") String message,
                          @RequestParam(value = "conversionID",required = false)String conversionID,
                       HttpServletResponse response){
        response.setCharacterEncoding("UTF-8");
        if(conversionID==null)
            conversionID= UUID.randomUUID().toString();
        return chatClient.chat(message,conversionID);

    }

    @GetMapping("/private/stream")
    public Flux<String> PrichatStream(@RequestParam("message") String message,
                                      @RequestParam(value = "conversionID",required = false)String conversionID,
                                      HttpServletResponse response){
        response.setCharacterEncoding("UTF-8");
        if(conversionID==null)
            conversionID= UUID.randomUUID().toString();
        return chatClient.streamChat(message,conversionID);

    }

}
