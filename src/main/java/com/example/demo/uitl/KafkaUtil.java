package com.example.demo.uitl;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
public class KafkaUtil {
    private AdminClient adminClient;
    public KafkaUtil(AdminClient adminClient) {
        this.adminClient = adminClient;
    }
    public void createTopic(String topicName, int partitions, short replicationFactor,Integer addnum,Map<String, String>m_config) throws ExecutionException, InterruptedException {
        NewTopic topic = new NewTopic(topicName, partitions, replicationFactor);
        topic.configs(m_config);
        adminClient.createTopics(List.of(topic)).all().get();
        if(addnum!=null){
            adminClient.createPartitions(Map.of(topicName, NewPartitions.increaseTo(partitions+addnum))).all().get();
        }
    }
}
