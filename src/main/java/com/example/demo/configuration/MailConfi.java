package com.example.demo.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.mail")
@Data
public class MailConfi {
    public  String username;
    public static final String REDIS_MAIL_KEY_PREFIX = "MAIL_";
    /**
     * redis中 mail code 的过期时间
     */
    public static final int EXPIRED_TIME = 50;

    /**
     * 邮件发送状态
     * 已加入消息队列，等待发送
     */
    public static final int MAIL_STATE_WAIT = 0;

    /**
     * 邮件发送状态
     * 发送成功
     */
    public static final int MAIL_STATE_OK = 1;

    /**
     * 邮件发送状态
     * 发送失败
     */
    public static final int MAIL_STATE_ERROR = 2;

    /**
     * 邮件状态中间字符
     */
    public static final String MAIL_STATE_MID_CHAR = "_";

}
