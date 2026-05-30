package com.example.demo.service.impl;

import com.example.demo.common.BaseContext;
import com.example.demo.configuration.RedisConfig;
import com.example.demo.mapper.BolgMapper;
import com.example.demo.mapper.DiscussMapper;
import com.example.demo.mapper.ReplyMapper;
import com.example.demo.mapper.userMapper;
import com.example.demo.model.pojo.Blog;
import com.example.demo.model.pojo.Discuss;
import com.example.demo.model.pojo.Reply;
import com.example.demo.model.pojo.User;
import com.example.demo.service.ReplyService;
import com.example.demo.uitl.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ReplyServiceImpl implements ReplyService {
    @Autowired
    private ReplyMapper replyMapper;
    @Autowired
    private userMapper userMapper;
    @Autowired
    private DiscussMapper discussMapper;
    @Autowired
    private DateUtil dateUtil;
    @Autowired
    private BolgMapper bolgMapper;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;


   @Transactional(rollbackFor = Exception.class)
    public void saveReply(Integer discussId, String replyBody, Integer rootId){
        User user=userMapper.selectById(BaseContext.getCurrentId().intValue());
        Reply reply=new Reply();
       Discuss discuss=discussMapper.selectById(discussId);
        if(discuss==null)
            throw new RuntimeException("评论不存在");
        reply.setDiscuss(discuss);
        reply.setUser(user);
        reply.setBody(replyBody);
        reply.setTime(dateUtil.getCurrentTime());
        if(rootId!=null&&rootId>0){
        Reply rootReply=new Reply();
        rootReply.setId(rootId);
        reply.setReply(rootReply);
        }
        replyMapper.saveReply(reply);
       Blog blog=bolgMapper.selectById(discuss.getBlog().getId());
       blog.setDiscussCount(blog.getDiscussCount()+1);
       bolgMapper.updateBlog(blog);
       redisTemplate.delete(RedisConfig.REDIS_BLOG_PREFIX+discuss.getBlog().getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteReply(Integer replyId) {
        User user=userMapper.selectById(BaseContext.getCurrentId().intValue());
        Reply reply=replyMapper.findReplyById(replyId);
        if(reply==null)
            throw new RuntimeException("回复不存在");
        if(!user.getId().equals(reply.getUser().getId()))
            throw new RuntimeException("无权删除");
        replyMapper.deleteReplyById(replyId);
        Blog blog=bolgMapper.selectById(reply.getDiscuss().getBlog().getId());
        blog.setDiscussCount(blog.getDiscussCount()-1);
        bolgMapper.updateBlog(blog);
        redisTemplate.delete(RedisConfig.REDIS_BLOG_PREFIX+reply.getDiscuss().getBlog().getId());
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void adminDeleteReply(Integer replyId) {
        Reply rely=replyMapper.findReplyById(replyId);
        if(rely==null)
            throw new RuntimeException("回复不存在");
        replyMapper.deleteReplyById(replyId);
        Discuss discuss=discussMapper.selectById(rely.getDiscuss().getId());
        Blog blog=bolgMapper.selectById(discuss.getBlog().getId());
        blog.setDiscussCount(blog.getDiscussCount()-1);
        bolgMapper.updateBlog(blog);
        redisTemplate.delete(RedisConfig.REDIS_BLOG_PREFIX+discuss.getBlog().getId());
    }
}
