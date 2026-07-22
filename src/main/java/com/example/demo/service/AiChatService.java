package com.example.demo.service;


import com.example.demo.common.BaseContext;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.mapper.SessionMapper;
import com.example.demo.model.pojo.Role;
import com.example.demo.model.pojo.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiChatService {

    private final ToolFactor toolFactor;
    private final ChatClient chatClient;
    private final RoleMapper roleMapper;
    private final SessionMapper sessionMapper;

    public AiChatService(ToolFactor toolFactor, ChatClient chatClient, RoleMapper roleMapper, SessionMapper sessionMapper) {
        this.toolFactor = toolFactor;
        this.chatClient = chatClient;
        this.roleMapper = roleMapper;
        this.sessionMapper = sessionMapper;
    }

    /**
     * 获取工具列表
     * @param
     * @return
     */
    private List<ToolCallback> getTools(){
        int userId= BaseContext.getCurrentId().intValue();
        Set<String> roles= roleMapper.selectRoleById(userId).stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());
        return toolFactor.getToolForRoles(roles);
    }

    private String getTitle(String message){
        String title = chatClient.prompt()
                .system("你是一个AI助手，请根据用户的问题用不超过20个字总结会话标题，只返回标题本身不要多余内容")
                .user(message)
                .call().content();
        log.info("会话标题:"+title);
        return title.length() > 255 ? title.substring(0, 255) : title;
    }



    /**
     * 非流式
     */
    public  String chat(String message,String conversationId){
        Session session=sessionMapper.findSessionById(conversationId);
        if(session==null){
            session=new Session(conversationId,BaseContext.getCurrentId().intValue(),getTitle(message),
                    1, LocalDateTime.now(),LocalDateTime.now());
               sessionMapper.insertSession(session);
        }
        else if(!session.getUserId().equals(BaseContext.getCurrentId().intValue()))
            throw new RuntimeException("无权限");
        List<ToolCallback>tools=getTools();
        return chatClient.prompt()
                .user(message)
                .options(DeepSeekChatOptions.builder().temperature(0.7).internalToolExecutionEnabled(true).toolChoice("auto").build())
                .toolCallbacks(tools)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID,conversationId))
                .call().content();
    }


    /**
     * 流式
     */
    public Flux<String> streamChat(String message,String conversationId){
        Session session=sessionMapper.findSessionById(conversationId);
        if(session==null){
            session=new Session(conversationId,BaseContext.getCurrentId().intValue(),getTitle(message),
                    1, LocalDateTime.now(),LocalDateTime.now());
            sessionMapper.insertSession(session);
        }
        else if(!session.getUserId().equals(BaseContext.getCurrentId().intValue()))
            throw new RuntimeException("无权限");
        List<ToolCallback>tools=getTools();
        return chatClient.prompt()
                .user(message)
                .options(DeepSeekChatOptions.builder().temperature(0.7).internalToolExecutionEnabled(true).toolChoice("required").build())
                .toolCallbacks(tools)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID,conversationId))
                .stream().content();
    }
}
