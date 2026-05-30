package com.example.demo.service;

public interface ReplyService {
    void saveReply(Integer discussId, String replyBody, Integer rootId);
    void deleteReply(Integer replyId);
    void adminDeleteReply(Integer replyId);
}
