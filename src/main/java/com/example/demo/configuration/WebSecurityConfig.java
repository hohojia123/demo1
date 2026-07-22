package com.example.demo.configuration;

import com.example.demo.interceptor.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {
    @Autowired
    private Interceptor interceptor;
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/sendMail",
                        "/user/forgetPassword",
                        "/user/logout",
                        "/blog/{blogId}/{isHistory}",
                        "/blog/home/**",
                        "/blog/hotBlog",
                        "/blog/statisticalBlogByMonth",
                        "/blog/searchBlog/**",
                        "/blog/getBlogLikeCount/**",
                        "/discuss/newDiscuss",
                        "/discuss/{blogId}/{page}/{showCount}",
                        "/blog/test",
                        "/rag/**"

                );
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF（前后端分离通常不需要）
                .csrf(csrf -> csrf.disable())
                // 2. 禁用表单登录和 Basic 认证
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // 3. 无状态会话（JWT 模式）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 4. 核心：定义授权规则
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }


}
