package com.example.demo.mapper;

import com.example.demo.model.entity.PageResult;
import com.example.demo.model.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface userMapper {



   User login(User user);

   void  updateUser(User user);

   User selectById(Integer id);

   User selectByName(String name);

   User selectByMail(String mail);

  void saveUser(User user);

    User findUserByMail(String newMail);

    List<User> findUserPage();

    List<User> searchUserByName(String userName);

    String getAvatar(int userid);
}
