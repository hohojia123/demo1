package com.example.demo.service.impl;

import com.example.demo.common.BaseContext;
import com.example.demo.configuration.RedisConfig;
import com.example.demo.mapper.BolgMapper;
import com.example.demo.mapper.UserLikeMapper;
import com.example.demo.mapper.userMapper;
import com.example.demo.model.pojo.User;
import com.example.demo.model.pojo.UserLike;
import com.example.demo.service.UserLikeService;
import com.example.demo.uitl.CacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserLikeServiceImpl implements UserLikeService {
    @Autowired
    private userMapper userMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserLikeMapper userLikemapper;
    @Autowired
    private BolgMapper bolgmapper;
    @Autowired
    private CacheUtil cacheUtil;
    @Override
    public boolean getUserLike(Integer id) {
        User user=userMapper.selectById(BaseContext.getCurrentId().intValue());
        String userLikeKey=getUserLikeKey(id,user.getId());
         int status=0;
         if(!redisTemplate.opsForHash().hasKey(RedisConfig.MAP_USER_LIKE_KEY,userLikeKey))
         {
             UserLike like=userLikemapper.getUserLike(id,user.getId());
             if(like!=null)
                 status=like.getStatus();
             redisTemplate.opsForHash().put(RedisConfig.MAP_USER_LIKE_KEY,userLikeKey,String.valueOf(status));
         }
         else
             status=Integer.parseInt((String)redisTemplate.opsForHash().get(RedisConfig.MAP_USER_LIKE_KEY,userLikeKey));
         return status!=0;

    }

    @Override
    public void saveUserLike(UserLike userLike) {
        User user=userMapper.selectById(BaseContext.getCurrentId().intValue());
        int bolgId=userLike.getBlog().getId();
        String userLikeKey=getUserLikeKey(bolgId,user.getId());
        redisTemplate.opsForHash().put(RedisConfig.MAP_USER_LIKE_KEY,userLikeKey,String.valueOf(userLike.getStatus()));
        int val=userLike.getStatus()==0?-1:1;
        cacheUtil.saveLikeWithMutex(RedisConfig.MAP_BLOG_LIKE_COUNT_KEY,RedisConfig.MAP_BLOG_LIKE_COUNT_KEY,"lock:blog"+bolgId,
                bolgId,val, id-> bolgmapper.getBlogLikeCountByBlogId(id), 7L, TimeUnit.DAYS);
    }

    public String getUserLikeKey(Integer blogId, Integer userId) {
        return blogId + RedisConfig.REDIS_LIKE_MID + userId;
    }

    @Transactional(rollbackFor = Exception.class)
    public void UserLikeFromRedistoDB(){
        List<UserLike> list=getLikeFromRedis();
        for(UserLike ul:list)
            userLikemapper.saveUserLike(ul);
    }

    @Override
    public List<UserLike> getLikeFromRedis() {
        List<UserLike>userLikeList= new ArrayList<>();
        try {
            Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(RedisConfig.MAP_USER_LIKE_KEY, ScanOptions.NONE);
            while (cursor.hasNext()) {
                Map.Entry<Object, Object> m = cursor.next();
                String key = (String) m.getKey();
                String[] keyArr = key.split(RedisConfig.REDIS_LIKE_MID);
                int blogId = Integer.parseInt(keyArr[0]);
                int userId = Integer.parseInt(keyArr[1]);
                int status = Integer.parseInt((String) m.getValue());
                UserLike userLike = new UserLike(blogId, userId, status);
                userLikeList.add(userLike);
                redisTemplate.opsForHash().delete(RedisConfig.MAP_USER_LIKE_KEY, key);
            }
            cursor.close();
        }catch (Exception e)
            {
                e.printStackTrace();
            }
        return userLikeList;
        }

}
