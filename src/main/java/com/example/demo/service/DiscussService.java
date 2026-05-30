package com.example.demo.service;

import com.example.demo.model.pojo.Discuss;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface DiscussService {
    void saveDiscuss(String discussBody, Integer bolgId);
     void deleteDiscuss(Integer discussId);
   void adminDeleteDiscuss(Integer discussId);
    PageInfo<Discuss> findDiscussByBlogId(Integer blogId, Integer page, Integer showCount);
    Long getDiscussCountByBlogId(Integer blogId);
    List<Discuss> findNewDiscuss();
    List<Discuss> findUserNewDiscuss();
}
