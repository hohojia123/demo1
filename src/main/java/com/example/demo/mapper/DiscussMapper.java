package com.example.demo.mapper;

import com.example.demo.model.pojo.Discuss;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DiscussMapper {
    @Insert("insert into discuss(discuss_body,discuss_time,blog_id,user_id) values(#{body},#{time},#{blog.id},#{user.id})")
    void saveDiscuss(Discuss discuss);

    Discuss selectById(Integer discussId);
@Delete("delete from discuss where discuss_id=#{discussId}")
    void deleteDiscusById(Integer discussId);

    List<Discuss> findDiscussByBlogId(Integer blogId);

@Select("SELECT count(1) FROM discuss WHERE blog_id = #{blogId}")
    Long getDiscussCountByBlogId(Integer blogId);

    List<Discuss> findNewDiscuss(int value);

    List<Discuss> findUserNewDiscuss(Integer id, int count);
}
