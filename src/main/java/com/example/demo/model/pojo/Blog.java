package com.example.demo.model.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 博文
 */
@Data
@ToString(exclude = "body")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Blog implements Serializable {
    /**
     * blog(36) => 541312(10)
     */
    private static final long serialVersionUID = 541312L;
    /**
     * id
     */
    private Integer id;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String body;

    /**
     * 评论数
     */
    private Integer discussCount;

    /**
     * 浏览数
     */
    private Integer blogViews;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime time;
    /**
     * 博文状态--0删除 1正常
     */
    private Integer state;

    /**
     * 所属用户
     */
    private User user;
    /**
     * 博文对应的标签
     */
    private List<Tag> tags;

    /**
     * 博文点赞数
     */
    private Integer likeCount;

    public Blog(int blogId, int likeCount) {
        this.id = blogId;
        this.likeCount = likeCount;
    }
}
