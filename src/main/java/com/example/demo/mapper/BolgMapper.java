package com.example.demo.mapper;

import com.example.demo.model.pojo.Blog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface BolgMapper {
  @Select("SELECT like_count FROM blog WHERE blog_id = #{blogId} and blog_state=1")
    int getBlogLikeCountByBlogId(int bolgId);

  Blog selectById(Integer bolgId);

  void updateBlog(Blog bolg);

    int findBlogCountByTagId(Integer tagId);

  void saveBlog(Blog blog);

  void saveBlogTag(Integer blogId, Integer tagId);

  List<Blog> selectByUserId(Integer id);

  Long getBlogCountByUserId(int i);

  Long getHomeBlogCount();

  List<Blog> selectHotBlog(int i);

  List<Blog> searchBlog(String searchText);

  Long getSearchBlogCount(String searchText);

  List<Blog> findAllBlog();

  Long getSearchAllBlogCount(String searchText);

  List<Blog> searchAllBlog(String searchText);

  List<Map> statisticalBlogByMonth(Integer value);
@Select("SELECT COUNT(*) FROM blog")
  Long getAllBlogCount();

  void updateLikeCount(Integer id, int count);

  List<Blog> findHomeBlog(int start, int showCount);
}
