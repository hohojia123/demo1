package com.example.demo.service.impl;


import cn.hutool.json.JSONUtil;
import com.example.demo.common.BaseContext;
import com.example.demo.configuration.JwtConfi;
import com.example.demo.configuration.MailConfi;
import com.example.demo.exception.UsernameNotFoundException;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.mapper.userMapper;
import com.example.demo.model.entity.PageResult;
import com.example.demo.model.pojo.Role;
import com.example.demo.model.pojo.User;
import com.example.demo.service.userService;
import com.example.demo.uitl.JwtUtil;
import com.example.demo.uitl.RandomUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class userServiceImpl implements userService {
    @Autowired
    private userMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private JwtConfi jwtConfig;
    @Autowired
    private RandomUtil randomUtil;
    @Autowired
    private MailSender mailSender;
    @Autowired
    private MailConfi mailConfi;
    @Autowired
    private RoleServiceImpl roleService;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private KafkaTemplate<String, String>kafkaTemplate;
    @Override
    public Map<String, Object> login(User user) {
       User user1= userMapper.login(user);
       if(user1==null)
           throw new UsernameNotFoundException("用户不存在");
        if (!encoder.matches(user.getPassword(), user1.getPassword())) {
            throw new UsernameNotFoundException("登录失败，密码错误");
        }  if(user1.getState()==0)
            throw new RuntimeException("用户被禁用");
       
       List<Role> roles = roleMapper.selectRoleById(user1.getId());
       user1.setRoles(roles);
       
       Map<String,Object>map=new HashMap<>();
        HashMap<String,Object> claims=new HashMap<>();
        claims.put("userId",user1.getId());
        claims.put("random", UUID.randomUUID());
        String token=jwtUtil.createJwt(claims);
        map.put("token","Bearer "+token);
        map.put("user",user1);
        redisTemplate.opsForValue().
                set(jwtConfig.REDIS_TOKEN_KEY_PREFIX + user1.getId(),  token, jwtConfig.getTtlMail(), TimeUnit.SECONDS);
        System.out.println(redisTemplate.keys("*"));
        return map;
    }

    @Override
    public void loginOut() {
        String key=jwtConfig.REDIS_TOKEN_KEY_PREFIX+ BaseContext.getCurrentId();
        redisTemplate.delete( key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(User user, String mailCode)throws RuntimeException {
             if(!checkMailCode(user.getMail(),mailCode))
                 throw new RuntimeException("验证码错误");
             String name=user.getName();
             String mail=user.getMail();
             if(userMapper.selectByName(name)!=null)
                 throw new RuntimeException("用户名已存在");
             if(userMapper.selectByMail(mail)!=null)
                 throw new RuntimeException("邮箱已存在");
             List<Role> roles=new ArrayList<Role>(1);
             roles.add(roleService.findRoleByName("USER"));
             user.setRoles(roles);
             user.setState(1);
             String password=user.getPassword();
             user.setPassword(encoder.encode(password));
             userMapper.saveUser(user);
             for(Role role:roles){
                 roleService.saveRole(role.getId(),user.getId());
             }
    }
    @Override
    public void updateUserState(Integer id, Integer state) {
              User user=new User();
               if(userMapper.selectById(id)==null)
                   throw new UsernameNotFoundException("用户不存在");
              user.setId(id);
              user.setState(state);
              userMapper.updateUser(user);
              redisTemplate.delete(jwtConfig.REDIS_TOKEN_KEY_PREFIX+id);
    }

    @Override
    public String getmailCodeFromRedis(String mail) {
        return redisTemplate.opsForValue().get(MailConfi.REDIS_MAIL_KEY_PREFIX+mail);
    }
    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000))
    @KafkaListener(topics = "email-send-topic",groupId = "email-send-group")
    public void sendMailAsync(ConsumerRecord<String, String>record, Acknowledgment ack) {
        Map<String, Object> m = JSONUtil.toBean(record.value(), Map.class);
        String mail = (String) m.get("email");
        String code = (String) m.get("code");
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailConfi.getUsername());
            message.setTo(mail);
            message.setSubject("博客系统验证码");
            message.setText("您的验证码是：" + code + "，有效期"+MailConfi.EXPIRED_TIME+"分钟");
            mailSender.send(message);
            ack.acknowledge();
            // 后台发送，不影响用户响应
        } catch (Exception e) {
            // 记录日志，但不影响用户
            System.err.println("邮件发送失败：" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    @Override
    public void sendMail(String mail) {
        Integer random = randomUtil.nextInt(100000, 999999);
        String code = random.toString();
        // 1. 先存入 Redis
        updateMailSendState(mail, code);
        Map<String,String>message=new HashMap<>();
        message.put("mail",mail);
        message.put("code",code);
        kafkaTemplate.send("email-send-topic",null, JSONUtil.toJsonStr( message));

    }
    public void updateMailSendState(String mail, String code) {
        String key = MailConfi.REDIS_MAIL_KEY_PREFIX + mail;
        // 存入 Redis，有效期 5 分钟
        redisTemplate.opsForValue().set(key, code, MailConfi.EXPIRED_TIME, TimeUnit.MINUTES);
    }

    @Override
    public boolean checkMailCode(String mail, String code) {
        String mailCode=redisTemplate.opsForValue().get(MailConfi.REDIS_MAIL_KEY_PREFIX+mail);
        if(mailCode==null)
            throw new RuntimeException("验证码不存在");
        return code.equals(mailCode);

    }

    @Override
    public String findUserMail() {
        return userMapper.selectById(BaseContext.getCurrentId().intValue()).getMail();
    }

    @Override
    public void updatePassword(String oldPassword, String newPassword, String code) {
               User user=userMapper.selectById(BaseContext.getCurrentId().intValue());
               if(!encoder.matches(oldPassword,user.getPassword()))
                   throw new RuntimeException("密码错误");
               if(!checkMailCode(user.getMail(),code))
                   throw new RuntimeException("验证码错误");
               user.setPassword(encoder.encode(newPassword));
               userMapper.updateUser(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserMail(String newMail,String MailCode){
        User user=userMapper.selectById(BaseContext.getCurrentId().intValue());
        if(!checkMailCode(user.getMail(), MailCode))
            throw new RuntimeException("邮箱验证码错误");
        if(userMapper.findUserByMail(newMail)!=null)
            throw new RuntimeException("此邮箱已使用");
        user.setMail(newMail);
        userMapper.updateUser(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void forgetPassword(String userName, String mailCode, String newPassword) {
        User user=userMapper.selectByName(userName);
        if(user==null)
            throw new RuntimeException("用户名不存在");
        if(!checkMailCode(user.getMail(), mailCode))
            throw new RuntimeException("验证码无效");
        user.setPassword(encoder.encode(newPassword));
        userMapper.updateUser(user);
    }

    @Override
    public PageResult<User> findUserPage(Integer page, Integer showCount) {
        PageHelper.startPage(page,showCount);
        List<User> users=userMapper.findUserPage();
        return new PageResult<>(PageInfo.of( users));
    }

    @Override
    public PageResult<User> searchUserByName(String userName,Integer page, Integer showCount) {
        PageHelper.startPage(page,showCount);
        List<User> users=userMapper.searchUserByName(userName);
        return new PageResult<>(PageInfo.of( users));
    }

    @Override
    public String getUserAvatar() {
        return userMapper.getAvatar(BaseContext.getCurrentId().intValue());
    }

    @Override
    public void updateAvatar(String avatarPath) {
               User user=new User();
               user.setId(BaseContext.getCurrentId().intValue());
               user.setAvatar(avatarPath);
               userMapper.updateUser(user);
    }

}
