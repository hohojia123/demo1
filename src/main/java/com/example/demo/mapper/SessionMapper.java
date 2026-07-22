package com.example.demo.mapper;


import com.example.demo.model.pojo.Session;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SessionMapper {

    Session findSessionById(String id);

    List<Session> findSessionByUserId(Integer userId);

    int insertSession(Session session);

    int updateSession(Session session);

    int deleteSession(String id);
}
