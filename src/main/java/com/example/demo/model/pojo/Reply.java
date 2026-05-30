package com.example.demo.model.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 回复
 */
@Data
@ToString

public class Reply {
    private Integer id;//id
    private String body;//回复内容
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime time;//回复时间
    private User user;//用户
    private Discuss discuss;//评论
    private Reply reply;//父节点回复




}
