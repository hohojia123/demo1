package com.example.demo.model.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 评论
 */
@Data
@ToString
public class Discuss {
    private Integer id;//id
    private String body;//评论内容
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime time;//评论时间
    private User user;//评论用户
    private Blog blog;//评论博文
    private List<Reply> replyList;



}
