package com.example.demo.service;


import com.example.demo.model.entity.PageResult;
import com.example.demo.model.pojo.User;

import java.util.Map;

public interface userService {

    Map<String, Object> login(User user);

    void loginOut();

    void register(User user, String mailCode);

    void updateUserState(Integer id, Integer state);

    String getmailCodeFromRedis(String mail);

    void sendMail(String mail);

    void updateMailSendState(String mail, String code);

    boolean checkMailCode(String mail, String code);

    String findUserMail();

    void updatePassword(String oldPassword, String newPassword, String code);

    void updateUserMail(String newMail, String MailCode);

    void forgetPassword(String userName, String mailCode, String newPassword);

    PageResult<User> findUserPage(Integer page, Integer showCount);

    PageResult<User> searchUserByName(String name,Integer page, Integer showCount);

    String getUserAvatar();

    void updateAvatar(String avatarPath);
}

