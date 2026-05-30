package com.example.demo.uitl;

import com.example.demo.configuration.JwtConfi;
import com.example.demo.model.pojo.User;
import com.example.demo.service.userService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Autowired
    private JwtConfi jwtConfi;

    public  String createJwt( HashMap<String,Object>claims){
        SecretKey key = Keys.hmacShaKeyFor(jwtConfi.getSecretKey().getBytes(StandardCharsets.UTF_8));

        long expTime=System.currentTimeMillis()+ jwtConfi.getTtlMail();
         Date exp=new Date(expTime);
         return Jwts.builder()
                 .claims(claims)
                 .expiration(exp)
                 .signWith(key)
                 .compact();

    }

    public HashMap<String, Object> parseJwt(String token) {
        // 去除 token 中的所有空白字符（空格、换行等）
        if (token != null) {
            token = token.replaceAll("\\s+", "");
        }
        
        SecretKey key = Keys.hmacShaKeyFor(jwtConfi.getSecretKey().getBytes(StandardCharsets.UTF_8));
        // 1. 先获取 Claims 对象（不要直接强转）
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        // 2. 将 Claims 转换为 HashMap
        HashMap<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }




}
