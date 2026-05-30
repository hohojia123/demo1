package com.example.demo.service.impl;

import com.example.demo.common.BaseContext;
import com.example.demo.mapper.BolgMapper;
import com.example.demo.mapper.TagMapper;
import com.example.demo.mapper.userMapper;
import com.example.demo.model.pojo.Tag;
import com.example.demo.model.pojo.User;
import com.example.demo.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {
  @Autowired
  private userMapper usermapper;
   @Autowired
   private TagMapper tagMapper;

   @Autowired
   private BolgMapper bolgMapper;

  @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveTag(String tagName) {
         User user=usermapper.selectById(BaseContext.getCurrentId().intValue());
         String username=user.getName();
         if(tagMapper.selectByTagName(tagName) != null)
           throw new RuntimeException("标签重复");
         Tag tag=new Tag();
         tag.setUser( user);
         tag.setName(tagName);
         tagMapper.saveTag(tag);
    }


     @Transactional(rollbackFor = Exception.class)
    public  void deleteTagById(Integer tagId){
         User user=usermapper.selectById(BaseContext.getCurrentId().intValue());
         Tag tag=tagMapper.selectById(tagId);
         if(!user.getId().equals(tag.getUser().getId()))
            throw new RuntimeException("无权删除此标签");
         if(bolgMapper.findBlogCountByTagId(tagId)>0)
           throw new RuntimeException("此标签关联了博客");
         tagMapper.deleteTagById(tagId);
     }


    @Transactional(rollbackFor = Exception.class)
     @Override
     public void updateTag(Integer tagId, String tagName) {
             User user=usermapper.selectById(BaseContext.getCurrentId().intValue());
             Tag tag=tagMapper.selectById(tagId);
             if(!user.getId().equals(tag.getUser().getId()))
                  throw new RuntimeException("无权修改此标签");
             tag.setName(tagName);
             tagMapper.updateTag(tag);
     }

    public List<Tag> findTagByUserId() {
       User user=usermapper.selectById(BaseContext.getCurrentId().intValue());
        return tagMapper.findTagByUserId(user.getId());
    }



}
