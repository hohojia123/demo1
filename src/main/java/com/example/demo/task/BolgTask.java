package com.example.demo.task;


import com.example.demo.configuration.RedisConfig;
import com.example.demo.mapper.BolgMapper;
import com.example.demo.mapper.TagMapper;
import com.example.demo.model.pojo.Blog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
@EnableScheduling
public class BolgTask {
 @Autowired
 private RedisTemplate<String,String> redisTemplate;
 @Autowired
 private BolgMapper blogMapper;
 @Autowired
 private TagMapper tagMapper;
 @Autowired
 private ObjectMapper objectMapper;
    @Scheduled(fixedRate = 1000 * 60 * 10)
    private void blogTask() throws JsonProcessingException {
            updateRedisHotBlogList();
    }
    public void updateRedisHotBlogList() throws JsonProcessingException {
         if(redisTemplate.hasKey(RedisConfig.REDIS_HOT_BLOG)&&redisTemplate.hasKey(RedisConfig.REDIS_NEW_BLOG)){
            List< String> hotBlogIds= redisTemplate.opsForList().range(RedisConfig.REDIS_HOT_BLOG, 0, RedisConfig.REDIS_HOT_BLOG_COUNT - 1);
            List< String> newBlogIds= redisTemplate.opsForList().range(RedisConfig.REDIS_NEW_BLOG, 0, RedisConfig.REDIS_NEW_BLOG_COUNT - 1);
            for(String blogId:hotBlogIds){
                if(!newBlogIds.contains(blogId))
                    redisTemplate.delete(RedisConfig.REDIS_BLOG_PREFIX + blogId);
            }
        }
         redisTemplate.delete(RedisConfig.REDIS_HOT_BLOG);
         List<Blog> hotBlogs=blogMapper.selectHotBlog(RedisConfig.REDIS_HOT_BLOG_COUNT);
          for(Blog blog:hotBlogs){
              blog.setTags(tagMapper.findTagByUserId(blog.getUser().getId()));
              String blogId= Integer.toString(blog.getId());
              redisTemplate.opsForList().rightPush(RedisConfig.REDIS_HOT_BLOG, blogId);
              redisTemplate.opsForValue().set(RedisConfig.REDIS_BLOG_PREFIX + blogId.toString(), objectMapper.writeValueAsString(blog));
          }
    }
       public void updateRedisNewBlogList() throws JsonProcessingException {
           if(redisTemplate.hasKey(RedisConfig.REDIS_NEW_BLOG)&&redisTemplate.hasKey(RedisConfig.REDIS_HOT_BLOG)){
               List<String> hotBlogIds= redisTemplate.opsForList().range(RedisConfig.REDIS_HOT_BLOG, 0, RedisConfig.REDIS_HOT_BLOG_COUNT - 1);
               List<String> newBlogIds= redisTemplate.opsForList().range(RedisConfig.REDIS_NEW_BLOG, 0, RedisConfig.REDIS_NEW_BLOG_COUNT - 1);
               for(String blogid:newBlogIds){
                   if(!hotBlogIds.contains(blogid))
                       redisTemplate.delete(RedisConfig.REDIS_BLOG_PREFIX + blogid);
               }
           }
           redisTemplate.delete(RedisConfig.REDIS_NEW_BLOG);
           List<Blog> newBlogs=blogMapper.findHomeBlog(0,RedisConfig.REDIS_NEW_BLOG_COUNT);
           for(Blog blog:newBlogs){
               blog.setTags(tagMapper.findTagByUserId(blog.getUser().getId()));
               String blogId= Integer.toString(blog.getId());
               redisTemplate.opsForList().rightPush(RedisConfig.REDIS_NEW_BLOG, blogId);
               redisTemplate.opsForValue().set(RedisConfig.REDIS_BLOG_PREFIX + blogId.toString(), objectMapper.writeValueAsString(blog));
           }
       }


}
