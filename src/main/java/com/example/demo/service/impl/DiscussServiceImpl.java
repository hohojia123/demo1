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
import com.example.demo.service.DiscussService;
import com.example.demo.uitl.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import java.util.List;

@Service
public class DiscussServiceImpl implements DiscussService {
    @Autowired
    private DiscussMapper discussMapper;
    @Autowired
    private userMapper userMapper;
    @Autowired
    private BolgMapper bolgmapper;
    @Autowired
    private DateUtil dateUtil;
    @Autowired
    private ReplyMapper replymapper;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveDiscuss(String discussBody, Integer bolgId) {
      User user=userMapper.selectById(BaseContext.getCurrentId().intValue());
      Blog bolg=bolgmapper.selectById(bolgId);
        Discuss discuss=new Discuss();
        discuss.setBlog(bolg);
        discuss.setUser(user);
        discuss.setBody(discussBody);
        discuss.setTime(dateUtil.getCurrentTime());
        discussMapper.saveDiscuss(discuss);
        bolg.setDiscussCount(bolg.getDiscussCount()+1);
        bolgmapper.updateBlog(bolg);
        redisTemplate.delete(RedisConfig.REDIS_BLOG_PREFIX+bolgId);

    }


    @Transactional(rollbackFor = Exception.class)
    public void deleteDiscuss(Integer discussId){
        User user=userMapper.selectById(BaseContext.getCurrentId().intValue());
        Discuss discuss=discussMapper.selectById(discussId);
        if(discuss==null)
            throw new RuntimeException("评论不存在");
        if(!user.getId().equals(discuss.getUser().getId()))
            throw new RuntimeException("无权删除");
        discussMapper.deleteDiscusById(discussId);
        Integer rows=replymapper.deleteReplyByDiscussId(discussId);
        Blog blog=bolgmapper.selectById(discuss.getBlog().getId());
        blog.setDiscussCount(blog.getDiscussCount()-1-rows);
        bolgmapper.updateBlog(blog);
        redisTemplate.delete(RedisConfig.REDIS_BLOG_PREFIX+discuss.getBlog().getId());
    }



    public void adminDeleteDiscuss(Integer discussId){
        Discuss discuss=discussMapper.selectById(discussId);
        if(discuss==null)
            throw new RuntimeException("评论不存在");
        discussMapper.deleteDiscusById(discussId);
        Integer rows= replymapper.deleteReplyByDiscussId(discussId);
        Blog blog=bolgmapper.selectById(discuss.getBlog().getId());
        blog.setDiscussCount(blog.getDiscussCount()-1-rows);
        bolgmapper.updateBlog(blog);
        redisTemplate.delete(RedisConfig.REDIS_BLOG_PREFIX+discuss.getBlog().getId());
    }



    @Transactional(readOnly = true)
  @Override
  public  PageInfo<Discuss>findDiscussByBlogId(Integer blogId, Integer page, Integer showCount) {
      PageHelper.startPage(page,showCount);
      List<Discuss> discusses = discussMapper.findDiscussByBlogId(blogId);
      for(Discuss discuss:discusses){
          List<Reply>replyList=replymapper.findReplyByDiscussId(discuss.getId());
          for(Reply reply:replyList){
              if(reply.getReply()!=null)
                  reply.setReply(replymapper.findReplyById(reply.getReply().getId()));
          }
          discuss.setReplyList(replyList);
      }
      return PageInfo.of(discusses);
  }

    public Long getDiscussCountByBlogId(Integer blogId) {
        return discussMapper.getDiscussCountByBlogId(blogId);
    }

    public List<Discuss> findNewDiscuss() {
        return discussMapper.findNewDiscuss(6);
    }

    public List<Discuss> findUserNewDiscuss() {
        return discussMapper.findUserNewDiscuss(BaseContext.getCurrentId().intValue(), 6);
    }
}
