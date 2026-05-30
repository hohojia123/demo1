package com.example.demo.service;

import com.example.demo.model.pojo.UserLike;

import java.util.List;

public interface UserLikeService {
    boolean getUserLike(Integer id);

    void saveUserLike(UserLike userLike);
    public void UserLikeFromRedistoDB();
    List<UserLike> getLikeFromRedis();
}
