package com.example.demo.configuration;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Configuration
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaConfig {

    private String bootstrapServers;

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    @Bean
    public AdminClient getAdmin() {
        return AdminClient.create(Map.of("bootstrap.servers", bootstrapServers));
    }



}
