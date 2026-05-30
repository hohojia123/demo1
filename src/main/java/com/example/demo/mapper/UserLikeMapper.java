package com.example.demo.mapper;

import com.example.demo.model.pojo.UserLike;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserLikeMapper {

    UserLike getUserLike(Integer id, Integer id1);
 void saveUserLike(UserLike ul);
}
