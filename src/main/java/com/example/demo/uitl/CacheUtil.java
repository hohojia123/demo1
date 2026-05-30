package com.example.demo.uitl;


import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.cell.CellSetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.function.Function;

@Component
public class CacheUtil {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static  final ExecutorService CACHE_THREAD_POOL = new ThreadPoolExecutor(3,5,10,TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(4), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());

    public void setExpire(String key,Object value,Long time,TimeUnit  unit){
        RedisData r=new RedisData();
        r.setData(value);
        r.setExpireTime(System.currentTimeMillis()+unit.toMillis(time));
        redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(r));
    }

    public <R> R queryWithPassThrough(String keyPrefix, Class<R>type, Integer id , Long time, TimeUnit unit, Function<Integer, R>db){
             String key=keyPrefix+id;
             String json=redisTemplate.opsForValue().get(key);
             if(StrUtil.isNotBlank( json)){
                 R res= JSONUtil.toBean(json,type);
                 return res;
             }
             if(json!=null)
                 return null;
             R r=db.apply(id);
             if(r==null) {
                 redisTemplate.opsForValue().set(key, "", time, unit);
                 return null;
             }
             redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(r), time, unit);
             return  r;
    }

    public <R> R queryWithLogicalExpire(String keyPrefix, String lockPrefix,Class<R> type, Integer id, Long time, TimeUnit unit, Function<Integer, R> db) {
            String key=keyPrefix+id;
            String json=redisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty())
            return null;
                RedisData redisData=JSONUtil.toBean(json,RedisData.class);
                JSONObject data= (JSONObject) redisData.getData();
                R r=JSONUtil.toBean(data,type);
                if(redisData.getExpireTime()>System.currentTimeMillis())
                    return r;
                boolean isLock=trylock(lockPrefix+id);
                    if(isLock)
                        CACHE_THREAD_POOL.execute(()->{  try {
                            R r1 = db.apply(id);
                            setExpire(key, r1, time, unit);
                        } finally {
                            unlock(lockPrefix+id);
                        }});
                    return r;
    }

    public<R> R queryWithMutex(String keyPrefix,String lockPrefix, Class<R> type, Integer id, Function<Integer,R> dbFallback, Long time, TimeUnit unit) {
        R r = null;
        String key = keyPrefix + id;
        while (true) {
            String shopjson = redisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(shopjson)) {
                r = JSONUtil.toBean(shopjson, type);
                return r;
            }
            if (shopjson != null)
                return null;
            try {
                boolean isLock = trylock(lockPrefix + id);
                if (!isLock) {
                    Thread.sleep(50);
                } else break;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            r = dbFallback.apply(id);
            if (r == null) {
                redisTemplate.opsForValue().set(key, "", 10L, TimeUnit.SECONDS);
                return null;
            }
            redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(r), time, unit);
        } finally {
            unlock(lockPrefix + id);
        }
        return r;
    }

    public void saveLikeWithMutex(String k, String keyPrefix, String lockPrefix, Integer id, int val, Function<Integer, Integer> dbFallback, Long time, TimeUnit unit) {
        if(Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(k, keyPrefix + id))) {
            redisTemplate.opsForHash().increment(k, keyPrefix + id, val);
            return;
        }
        boolean locked = trylock(lockPrefix + id);
        if (locked) {
            try {
                if (Boolean.FALSE.equals(redisTemplate.opsForHash().hasKey(k, keyPrefix + id))) {
                    Integer count = dbFallback.apply(id);
                    redisTemplate.opsForHash().put(k, keyPrefix + id, String.valueOf(count + val));
                } else {
                    redisTemplate.opsForHash().increment(k, keyPrefix + id, val);
                }
            } finally {
                unlock(lockPrefix + id);
            }
            return;
        }
        while (true){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            if(Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(k, keyPrefix + id))){
                redisTemplate.opsForHash().increment(k, keyPrefix + id, val);
                break;
            }
        }
    }

    public boolean trylock(String key){
        Boolean b=redisTemplate.opsForValue().setIfAbsent(key, "1",10L,TimeUnit.SECONDS);
        return BooleanUtil.isTrue(b);
    }
    public void unlock(String key){
        redisTemplate.delete(key);
    }
}
