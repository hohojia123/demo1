package com.example.demo.service.impl;

import com.example.demo.common.BaseContext;
import com.example.demo.configuration.RabbtiConfig;
import com.example.demo.configuration.RedisConfig;
import com.example.demo.mapper.BolgMapper;
import com.example.demo.mapper.TagMapper;
import com.example.demo.mapper.userMapper;
import com.example.demo.model.pojo.Blog;
import com.example.demo.model.pojo.Tag;
import com.example.demo.model.pojo.User;
import com.example.demo.service.BlogService;
import com.example.demo.task.BolgTask;
import com.example.demo.uitl.CacheUtil;
import com.example.demo.uitl.DateUtil;
import com.example.demo.uitl.FileUtil;
import com.example.demo.uitl.FormatUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class BlogServiceImpl implements BlogService {
 @Autowired
 private FormatUtil formatUtil;
 @Autowired
 private FileUtil fileUtil;
 @Autowired
 private userMapper userMapper;
 @Autowired
 private TagMapper tagMapper;
 @Autowired
 private BolgMapper bolgmapper;
 @Autowired
 private DateUtil dateUtil;
 @Autowired
 private StringRedisTemplate redisTemplate;
 @Autowired
 private ObjectMapper objectMapper;
 private static final int MAX_BODY_CHAR_COUNT = 150;
 @Autowired
 private BolgTask blogTask;
 @Autowired
 private RabbitTemplate rabbitTemplate;
 @Autowired
 private CacheUtil cacheUtil;

 private static  final DefaultRedisScript<Long>HOT_BLOG_LUA;
 static {
     HOT_BLOG_LUA = new DefaultRedisScript<>();
     HOT_BLOG_LUA.setLocation(new ClassPathResource("hot_blog.lua"));
     HOT_BLOG_LUA.setResultType(Long.class);
 }


 @Override
 public  synchronized String saveImg(MultipartFile file) throws Exception {
      return fileUtil.upload(file);
 }
    @Transactional(rollbackFor = Exception.class)
   public void saveBlog(String blogTitle, String blogBody, Integer[] tagIds) throws JsonProcessingException {
     User user=userMapper.selectById(BaseContext.getCurrentId().intValue());
     for (Integer tagId : tagIds) {
         // 通过标签id检查标签是否属于该用户
         if (!tagMapper.selectById(tagId).getUser().getId().equals(user.getId())) {
             throw new RuntimeException();
         }
     }
     Blog blog=Blog.builder()
             .user(user)
             .blogViews(0)
             .discussCount(0)
             .title(blogTitle)
             .body(blogBody)
             .state(1)
             .likeCount(0)
             .time(dateUtil.getCurrentTime()).build();
     bolgmapper.saveBlog(blog);
     for(Integer tagId:tagIds)
         bolgmapper.saveBlogTag(blog.getId(),tagId);
     redisTemplate.delete(RedisConfig.REDIS_STATISTICAL);
	    redisTemplate.delete(RedisConfig.REDIS_NEW_BLOG);
        blog.setTags(tagMapper.findTagByUserId(user.getId()));
     redisTemplate.execute(HOT_BLOG_LUA,Arrays.asList(RedisConfig.REDIS_HOT_BLOG),RedisConfig.REDIS_HOT_BLOG_COUNT+"",blog.getId().toString());
     blog.getUser().setPassword(null);
     blog.getUser().setMail(null);
     blog.getUser().setState(null);
     redisTemplate.opsForValue().set(RedisConfig.REDIS_BLOG_PREFIX + blog.getId().toString(), objectMapper.writeValueAsString(blog));

 }
 @Transactional(rollbackFor = Exception.class)
    @Override
    public Blog findBlogById(Integer blogId, boolean isHistory) throws IOException {
      Blog blog=cacheUtil.queryWithPassThrough(RedisConfig.REDIS_BLOG_PREFIX,Blog.class, blogId, 30L, TimeUnit.SECONDS,
              id -> {
                  Blog b = bolgmapper.selectById(id);
                  if (b == null) return null;
                  b.setTags(tagMapper.findTagByUserId(b.getUser().getId()));
                  return b;
              });
       if(isHistory){
           blog.setBlogViews(blog.getBlogViews() + 1);
               redisTemplate.opsForValue().set(RedisConfig.REDIS_BLOG_PREFIX + blogId, objectMapper.writeValueAsString(blog));
               // rabbitTemplate.convertAndSend(RabbtiConfig.BLOG_QUEUE, blog);
               bolgmapper.updateBlog(blog);
       }
       if(blog==null)
           throw new RuntimeException("博客不存在");
       return blog;
    }

    @Override
    public PageInfo<Blog> findBlogByUser(Integer page, Integer showCount) {
     User user=userMapper.selectById(BaseContext.getCurrentId().intValue());
        PageHelper.startPage(page,showCount);
        List<Blog> blogs=bolgmapper.selectByUserId(user.getId());
        for(Blog blog:blogs)
            blog.setTags(tagMapper.findTagByUserId(user.getId()));
            return PageInfo.of(blogs);
    }

    @Override
    public Long getBlogCountByUser() {
        return bolgmapper.getBlogCountByUserId(BaseContext.getCurrentId().intValue());
    }

    public Long getHomeBlogCount() {
        return bolgmapper.getHomeBlogCount();
    }

    @Override
    public List<Blog> findHotBlog() throws IOException {
        if(redisTemplate.hasKey(RedisConfig.REDIS_HOT_BLOG)){
            List<Blog>blogs=new ArrayList<>(6);
            List<String> blogIds=redisTemplate.opsForList().range(RedisConfig.REDIS_HOT_BLOG,0,RedisConfig.REDIS_HOT_BLOG_COUNT);
            for(String blogId:blogIds){
                String blogJson = (String) redisTemplate.opsForValue().get(RedisConfig.REDIS_BLOG_PREFIX + blogId);
                if (blogJson == null) {
                    continue; // 跳过不存在的数据
                }
                Blog blog=objectMapper.readValue(blogJson, Blog.class);
                blogs.add(blog);
            }
            return blogs;
        }
        else {
            return bolgmapper.selectHotBlog(6);
        }

    }

    @Override
    public PageInfo<Blog> searchBlog(String searchText, Integer page, Integer showCount) {
        PageHelper.startPage(page,showCount);
        List<Blog>blogs=bolgmapper.searchBlog(searchText);
        for(Blog blog:blogs)
            blog.setTags(tagMapper.findTagByUserId(blog.getUser().getId()));
        return PageInfo.of(blogs);
    }
    public Long getSearchBlogCount(String searchText) {
        return bolgmapper.getSearchBlogCount(searchText);
    }

    @Override
    public PageInfo<Blog> findAllBlog(Integer page, Integer showCount) {
     PageHelper.startPage(page,showCount);
      return PageInfo.of(bolgmapper.findAllBlog());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateBlog(Integer blogId, String blogTitle, String blogBody, Integer[] tagIds) throws JsonProcessingException {
           User user=userMapper.selectById(BaseContext.getCurrentId().intValue());
           Blog blog=bolgmapper.selectById(blogId);
           if(!user.getId().equals(blog.getUser().getId()))
               throw new RuntimeException("无权限修改");
           blog.setTitle(blogTitle);
           blog.setBody(blogBody);
           bolgmapper.updateBlog(blog);
           tagMapper.deleteTagByBlogId(blogId);
           for(Integer tagId:tagIds)
               bolgmapper.saveBlogTag(blogId,tagId);
           if(redisTemplate.hasKey(RedisConfig.REDIS_BLOG_PREFIX + blogId))
           {
               blog.setTags(tagMapper.findTagByUserId(user.getId()));
               redisTemplate.opsForValue().set(RedisConfig.REDIS_BLOG_PREFIX + blogId, objectMapper.writeValueAsString(blog));
           }
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBlog(Integer blogId) throws JsonProcessingException {
     Integer userid=BaseContext.getCurrentId().intValue();
           Blog blog=bolgmapper.selectById(blogId);
          if(!userid.equals(blog.getUser().getId()))
              throw new RuntimeException("无权限删除");
          blog.setState(0);
          bolgmapper.updateBlog(blog);
          tagMapper.deleteTagByBlogId(blogId);
          if(redisTemplate.hasKey(RedisConfig.REDIS_BLOG_PREFIX + blogId)) {
              blogTask.updateRedisNewBlogList();
              redisTemplate.delete(RedisConfig.REDIS_BLOG_PREFIX + blogId);
          }
          redisTemplate.delete(RedisConfig.REDIS_STATISTICAL);

    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void adminDeleteBlog(Integer blogId) throws JsonProcessingException {
        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setState(0);
        bolgmapper.updateBlog(blog);
        tagMapper.deleteTagByBlogId(blogId);
        if(redisTemplate.hasKey(RedisConfig.REDIS_BLOG_PREFIX + blogId)){
            List<String> newBlogIds = redisTemplate.opsForList().range(RedisConfig.REDIS_NEW_BLOG, 0, RedisConfig.REDIS_NEW_BLOG_COUNT - 1);
            List<String> hotBlogIds = redisTemplate.opsForList().range(RedisConfig.REDIS_HOT_BLOG, 0, RedisConfig.REDIS_HOT_BLOG_COUNT - 1);
            if (newBlogIds != null && newBlogIds.contains(blogId + "")) blogTask.updateRedisNewBlogList();
            if (hotBlogIds != null && hotBlogIds.contains(blogId + "")) blogTask.updateRedisHotBlogList();
        }
        redisTemplate.delete(RedisConfig.REDIS_STATISTICAL);
        redisTemplate.delete(RedisConfig.REDIS_BLOG_PREFIX + blogId);
    }

    public Long getSearchAllBlogCount(String searchText) {
        return bolgmapper.getSearchAllBlogCount(searchText);
    }

    @Override
    public PageInfo<Blog> searchAllBlog(String searchText, Integer page, Integer showCount) {
       PageHelper.startPage(page,showCount);
        return PageInfo.of(bolgmapper.searchAllBlog(searchText));
    }

    public List<Map> statisticalBlogByMonth() throws IOException{
     if(redisTemplate.hasKey(RedisConfig.REDIS_STATISTICAL)){
         String maoJson = (String) redisTemplate.opsForValue().get(RedisConfig.REDIS_STATISTICAL);
         if (maoJson == null) {
             // Redis 数据丢失，重新从数据库查询
             List<Map> maps=bolgmapper.statisticalBlogByMonth(6);
             redisTemplate.opsForValue().set(RedisConfig.REDIS_STATISTICAL, objectMapper.writeValueAsString(maps));
             return maps;
         }
         List<Map> list=objectMapper.readValue(maoJson, new TypeReference<>() {
         });
         return list;
     }
     else{
         List<Map> maps=bolgmapper.statisticalBlogByMonth(6);
         redisTemplate.opsForValue().set(RedisConfig.REDIS_STATISTICAL, objectMapper.writeValueAsString(maps));
         return maps;
     }
    }

    public Long getAllBlogCount() {
        return bolgmapper.getAllBlogCount();
    }
     public int getBlogLikeCountByBlogId(Integer blogId) {
     int likeCount;
     String likeCountKey=String.valueOf(blogId);
     if(!redisTemplate.opsForHash().hasKey(RedisConfig.MAP_BLOG_LIKE_COUNT_KEY,likeCountKey)){
         likeCount= bolgmapper.getBlogLikeCountByBlogId(blogId);
         redisTemplate.opsForHash().put(RedisConfig.MAP_BLOG_LIKE_COUNT_KEY, likeCountKey, String.valueOf(likeCount));
     }else
         likeCount= Integer.parseInt((String)redisTemplate.opsForHash().get(RedisConfig.MAP_BLOG_LIKE_COUNT_KEY, likeCountKey));
      return likeCount;
     }


     @Transactional(rollbackFor = Exception.class)
    @Override
    public void transLikeCountFromRedis2DB() {
            List<Blog> blogList=getBlogLikeCountFromRedis();
            for(Blog blog:blogList){
                int count=blog.getLikeCount();
                bolgmapper.updateLikeCount(blog.getId(),count);
            }
    }
     public List<Blog> getBlogLikeCountFromRedis(){
         List<Blog> blogList = new ArrayList<>();
         try {
             Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(RedisConfig.MAP_BLOG_LIKE_COUNT_KEY, ScanOptions.NONE);
         while (cursor.hasNext()) {
             Map.Entry<Object, Object> entry = cursor.next();
             String key = (String) entry.getKey();
             // 从键中提取数字部分，假设键格式为 "MAP_BLOG_LIKE_COUNT{数字}"
             String numericPart = key.replaceAll("[^0-9]", "");
             int blogId = Integer.parseInt(numericPart);
             int count = Integer.parseInt((String) entry.getValue());
             Blog blog = new Blog(blogId, count);
             blogList.add(blog);
             redisTemplate.opsForHash().delete(RedisConfig.MAP_BLOG_LIKE_COUNT_KEY, key);
         }
         cursor.close();
     }catch(Exception e){
         e.printStackTrace();
     }
     return blogList;
 }

 public PageInfo<Blog> findHomeBlog(Integer page, Integer showCount) throws IOException{
     int start=(page-1)*showCount;
     if(!redisTemplate.hasKey(RedisConfig.REDIS_NEW_BLOG)){
         List<Blog> blogMysql=bolgmapper.findHomeBlog(0, RedisConfig.REDIS_NEW_BLOG_COUNT);
         for(Blog blog:blogMysql){
             blog.setTags(tagMapper.findTagByUserId(blog.getUser().getId()));
             String blogId= Integer.toString(blog.getId());
             redisTemplate.opsForList().rightPush(RedisConfig.REDIS_NEW_BLOG, blogId);
             redisTemplate.opsForValue().set(RedisConfig.REDIS_BLOG_PREFIX + blogId, objectMapper.writeValueAsString(blog));
         }
     }
     List<Blog>blogs=new ArrayList<>();
     if(start>=RedisConfig.REDIS_NEW_BLOG_COUNT){
         blogs.addAll(bolgmapper.findHomeBlog(start, showCount));
         for(Blog blog:blogs)
             blog.setTags(tagMapper.findTagByUserId(blog.getUser().getId()));
     }else if(start+showCount>RedisConfig.REDIS_NEW_BLOG_COUNT){
         List<String>redisBlogIds=redisTemplate.opsForList().range(RedisConfig.REDIS_NEW_BLOG,start,RedisConfig.REDIS_NEW_BLOG_COUNT-1);
          for(String blogId:redisBlogIds){
              String blogJson = (String) redisTemplate.opsForValue().get(RedisConfig.REDIS_BLOG_PREFIX + blogId);
              if (blogJson == null) {
                  continue; // 跳过不存在的数据
              }
              Blog blog=objectMapper.readValue(blogJson, Blog.class);
              blogs.add(blog);
          }
          blogs.addAll(bolgmapper.findHomeBlog(RedisConfig.REDIS_NEW_BLOG_COUNT, showCount - (RedisConfig.REDIS_NEW_BLOG_COUNT - start)));
     }else{
         List<String> redisBlogIds=redisTemplate.opsForList().range(RedisConfig.REDIS_NEW_BLOG,start,start+showCount-1);
         for(String blogId:redisBlogIds){
             String blogJson = (String) redisTemplate.opsForValue().get(RedisConfig.REDIS_BLOG_PREFIX + blogId);
             if (blogJson == null) {
                 continue; // 跳过不存在的数据
             }
             Blog blog=objectMapper.readValue(blogJson, Blog.class);
             blogs.add(blog);
         }
     }
     for(Blog blog:blogs){
         String body=blog.getBody();
         if(body.length()>MAX_BODY_CHAR_COUNT)
             blog.setBody(body.substring(0, MAX_BODY_CHAR_COUNT));
     }
    return PageInfo.of(blogs);

 }



}
