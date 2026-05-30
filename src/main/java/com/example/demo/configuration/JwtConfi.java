package com.example.demo.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.jwt")
@Data
public class JwtConfi {
    public static final String REDIS_TOKEN_KEY_PREFIX = "TOKEN_";
    private  String secretKey;
    private   long ttlMail;
    private String prefix;
    private String header;


}
