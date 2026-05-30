package com.example.demo.mapper;


import com.example.demo.model.pojo.Reply;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReplyMapper {

@Delete("delete from reply where discuss_id=#{discussId}")
    Integer deleteReplyByDiscussId(Integer discussId);

    List<Reply> findReplyByDiscussId(Integer id);

    Reply findReplyById(Integer id);

    void saveReply(Reply reply);
@Delete("delete from reply where reply_id=#{replyId}")
    void deleteReplyById(Integer replyId);
}
