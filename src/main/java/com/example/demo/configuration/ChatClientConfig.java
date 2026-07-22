package com.example.demo.configuration;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Schedulers;

@Configuration
public class ChatClientConfig {

    @Bean
   public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .build();
    }

    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory)
                .order(0)
                .scheduler(Schedulers.boundedElastic())
                .build();
    }

    @Bean
  public ChatClient setDeepSeekChat(DeepSeekChatModel deepSeekChatModel,MessageChatMemoryAdvisor messageChatMemoryAdvisor){
     return ChatClient.builder(deepSeekChatModel)
             .defaultSystem("你是该个人博客网站的AI助手。"
                 + "优先使用查询数据库工具查询数据库信息之后可以根据你自身知识一起回答用户。\n"
                 + "当用户询问相关信息时，调用最相关的几个工具来查询即可，不用把工具全部调用，按需求调用。\n"
                 + "根据工具返回的真实数据来回答，不要编造数据。\n"
                 + "如果没有任何可用工具能查询，才可以用自己的知识回答。\n"
                 + "不允许回答不合法问题，若有触及，用抱歉语句回答即可")
             .defaultAdvisors(new SimpleLoggerAdvisor(), messageChatMemoryAdvisor)
             .build();
  }


}
