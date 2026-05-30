package com.example.demo.service;

import com.example.demo.model.pojo.Tag;

import java.util.List;

public interface TagService {

    void saveTag(String tagName);
    void deleteTagById(Integer tagId);
    void updateTag(Integer tagId, String tagName);
    List<Tag> findTagByUserId();
}
