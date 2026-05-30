package com.example.demo.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbtiConfig {
    public static final String BLOG_QUEUE = "blog_views_queue";

    @Bean
    public Queue blogQueue() {
        return new Queue(BLOG_QUEUE, true);
    }
}
