package com.example.demo.uitl;

import com.example.demo.configuration.MailConfi;
import com.example.demo.mapper.BolgMapper;
import com.example.demo.model.pojo.Blog;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.EnableKafkaRetryTopic;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class Consumer {
    @Autowired
    private BolgMapper bolgmapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String BLOG_VIEW_BUFFER = "BLOG_VIEW_NUM";

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000))
    @KafkaListener(topics = "blog-topic",groupId="bloglike-group")
   public void ListenBlog(ConsumerRecord<String, Blog>record, Acknowledgment ack){
        try {
            String key = record.key();
            Blog blog = record.value();
            redisTemplate.opsForHash().increment(BLOG_VIEW_BUFFER,blog.getId().toString(),1L);
            int partition = record.partition();
            long offset = record.offset();
            System.out.println("分区:" + partition + " offset:" + offset);
            System.out.println(key);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("消费异常", e);
            throw new RuntimeException( e);
        }
    }

    @Scheduled(fixedRate = 1000 * 60)
    public  synchronized  void syncUpdateBlogview2DB() {
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("blog:view" , "1",10, TimeUnit.SECONDS);
        if (!lock) {
            log.info("已有实例消费");
            return;
        }
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(BLOG_VIEW_BUFFER);
            if (entries.isEmpty()) {
                log.debug("没有待同步的浏览量数据");
                return;
            }
            redisTemplate.delete(BLOG_VIEW_BUFFER);
            List<Blog>totaldata=new ArrayList<>();
            for(Map.Entry<Object, Object>entry:entries.entrySet()){
                Blog b=new Blog();
                b.setId(Integer.parseInt(entry.getKey().toString()));
                b.setBlogViews(Integer.parseInt(entry.getValue().toString()));
                totaldata.add(b);
            }
            if(!totaldata.isEmpty()) {
                bolgmapper.updateBlogViews(totaldata);
                log.info("更新了"+totaldata.size()+"条数据");
            }
        }catch (Exception e){
            log.error("同步数据异常",e);
        }finally {
            redisTemplate.delete("blog:view");
        }
    }
    @KafkaListener(topics = "order-topic",groupId="testgroup")
    public void test(ConsumerRecord<String,String>record){
        String key = record.key();
        String value = record.value();
        int partition = record.partition();
        long offset = record.offset();
        System.out.println("分区:" + partition + " offset:" + offset);
        System.out.println(key);
        System.out.println(value);
    }


}

