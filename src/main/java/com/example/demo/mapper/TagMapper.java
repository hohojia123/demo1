package com.example.demo.mapper;

import com.example.demo.model.pojo.Tag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagMapper {

    Tag selectByTagName(String tagName);

    void saveTag(Tag tag);

    Tag selectById(Integer tagId);

  @Delete("DELETE FROM tag WHERE tag_id = #{tagId}")
    void deleteTagById(Integer tagId);

    List<Tag> findTagByUserId(Integer id);

    void deleteTagByBlogId(Integer blogId);

    void updateTag(Tag tag);
}
