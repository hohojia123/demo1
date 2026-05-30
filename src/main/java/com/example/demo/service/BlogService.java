package com.example.demo.service;

import com.example.demo.model.pojo.Blog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface BlogService {
    //String saveImg(MultipartFile file)throws IOException;
    void saveBlog(String blogTitle, String blogBody, Integer[] tagIds) throws JsonProcessingException;
    String saveImg(MultipartFile file) throws Exception;
    Blog findBlogById(Integer blogId, boolean isHistory) throws IOException;
    PageInfo<Blog> findBlogByUser(Integer page, Integer showCount);
    Long getBlogCountByUser();
    Long getHomeBlogCount();
    List<Blog> findHotBlog() throws IOException;
    PageInfo<Blog> searchBlog(String searchText, Integer page, Integer showCount);
    Long getSearchBlogCount(String searchText);
    PageInfo<Blog> findAllBlog(Integer page, Integer showCount);
    PageInfo<Blog> searchAllBlog(String searchText, Integer page, Integer showCount);
    Long getSearchAllBlogCount(String searchText);
    void adminDeleteBlog(Integer blogId) throws JsonProcessingException;
    void deleteBlog(Integer blogId) throws JsonProcessingException;
    void updateBlog(Integer blogId, String blogTitle, String blogBody, Integer[] tagIds) throws JsonProcessingException;
    List<Map> statisticalBlogByMonth() throws IOException;
    Long getAllBlogCount();
    int getBlogLikeCountByBlogId(Integer blogId);
    void transLikeCountFromRedis2DB();
    List<Blog> getBlogLikeCountFromRedis();
    PageInfo<Blog> findHomeBlog(Integer page, Integer showCount) throws IOException;
}
