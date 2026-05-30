package com.example.demo.interceptor;


import com.example.demo.common.BaseContext;
import com.example.demo.common.RequireRole;
import com.example.demo.configuration.JwtConfi;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.mapper.userMapper;
import com.example.demo.model.pojo.Role;
import com.example.demo.model.pojo.User;
import com.example.demo.service.userService;
import com.example.demo.uitl.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.List;

@Component
public class Interceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private JwtConfi jwtConfig;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private userMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)throws  Exception{
       if (!(handler instanceof HandlerMethod)) {
        //当前拦截到的不是动态方法，直接放行
        return true;
    }
    String token = request.getHeader("Authorization");

      if (token==null||!token.startsWith("Bearer ")) {
          response.setStatus(401);
          return false;}
          token = token.substring(7);

        if (token.isEmpty() || !token.contains(".")) {
           response.setStatus(401);
           return false;
       }try{
        HashMap<String, Object>claim=jwtUtil.parseJwt(token);
        String key=jwtConfig.REDIS_TOKEN_KEY_PREFIX+claim.get("userId");
        String redisToken= (String) redisTemplate.opsForValue().get(key);
        if (redisToken==null||!token.equals(redisToken)) {
            response.setStatus(401);
            return false;
        }
           Long userId = ((Number) claim.get("userId")).longValue();

           BaseContext.setCurrentId(userId);
        RequireRole requireRole = ((HandlerMethod) handler).getMethodAnnotation(RequireRole.class);
         if(requireRole!=null){
             List<Role>roles= roleMapper.selectRoleById(userId.intValue());
             if (roles == null || roles.isEmpty()) {
                 response.setStatus(403);
                 return false;
             }
             String[] requiredRoles = requireRole.value();
             boolean hasRole = roles.stream()
                     .anyMatch(role -> {
                         for (String required : requiredRoles) {
                             if (required.equalsIgnoreCase(role.getName())) {
                                 return true;
                             }
                         }
                         return false;
                     });
             if (!hasRole) {
                 response.setStatus(403);
                 return false;
             }
         }
        return true;
    } catch (Exception e) {
        response.setStatus(401);
        e.printStackTrace();
        return false;
    }

}





}
