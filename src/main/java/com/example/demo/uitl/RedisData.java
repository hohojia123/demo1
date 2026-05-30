package com.example.demo.uitl;

import lombok.Data;

@Data
public class RedisData {
    private Long expireTime;
    private Object data;
}
