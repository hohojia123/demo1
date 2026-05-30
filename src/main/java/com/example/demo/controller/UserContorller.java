package com.example.demo.controller;

import com.example.demo.common.RequireRole;
import com.example.demo.configuration.MailConfi;
import com.example.demo.model.entity.PageResult;
import com.example.demo.model.entity.Result;
import com.example.demo.model.entity.StatusCode;
import com.example.demo.service.userService;
import com.example.demo.uitl.FormatUtil;
import com.example.demo.uitl.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.example.demo.model.pojo.User;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserContorller {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private FormatUtil formatUtil;
    @Autowired
    private userService userservice;


   @PostMapping("/login")
     public Result Login(@RequestParam String name, @RequestParam String password){
         if(!formatUtil.checkNull(name,password))
             return Result.error(StatusCode.ERROR,"用户名或密码错误");
         User user = new User();
         user.setName(name);
         user.setPassword(password);
         Map<String,Object> map=userservice.login(user);
         return Result.success("登录成功",map);
     }

     /**
      * 登出
      * @return
      */
     @GetMapping("/logout")
     public Result loginOut(){
         userservice.loginOut();
         return  Result.success("登出成功");
     }
     /**
      * 注册
      * @param
      * @return
      */
     @PostMapping("/register")
     public Result register(@RequestParam String name, @RequestParam String password, @RequestParam String mail, @RequestParam String mailCode){
         if (!formatUtil.checkNull(name,password,mail,mailCode))
                return Result.error(StatusCode.ERROR,"参数错误");
         User user = new User();
         user.setName(name);
         user.setPassword(password);
         user.setMail(mail);
         userservice.register(user, mailCode);
         return Result.success("注册成功");
     }
    @RequireRole("admin")
    @GetMapping("/ban/{id}/{state}")
    public Result banUser(@PathVariable Integer id, @PathVariable Integer state) {
          if(!formatUtil.checkObjectNull(id,state))
               return Result.error(StatusCode.ERROR,"参数错误");
               if(state==0) {
                   userservice.updateUserState(id, state);
                   return Result.success("封禁成功");
               }
               else {
                   userservice.updateUserState(id, state);
                   return Result.success("解封成功");
               }
           }


    @PostMapping("/sendMail")
    public Result sendMail(@RequestParam String mail) {
         if(!formatUtil.isMail(mail))
             return Result.error(StatusCode.ERROR,"邮箱格式错误");

         String mailCode=userservice.getmailCodeFromRedis(mail);
         if(mailCode!=null)
             return Result.error(StatusCode.ERROR, MailConfi.EXPIRED_TIME+" 分钟内不可重复发送");
         else{
             userservice.sendMail(mail);
             return Result.success("发送成功");
         }

    }


    @RequireRole("USER")
    @GetMapping("/mail")
    public  Result getUserMail(){
        return Result.success("查询成功",userservice.findUserMail());
    }


    @RequireRole("USER")
    @PostMapping("/updatePassword")
    public  Result updatePassword(String oldPassword, String newPassword, String code ){
        if(!formatUtil.checkObjectNull(oldPassword,newPassword,code))
            return Result.error(StatusCode.ERROR,"参数错误");
        try{
            userservice.updatePassword(oldPassword,newPassword,code);
            return Result.success("修改密码成功");
        }
        catch (RuntimeException e){
            return Result.error(StatusCode.ERROR,e.getMessage());
        }
    }

    @RequireRole("USER")
    @PostMapping("/updateMail")
    public Result updateMail(String newMail, String MailCode){
         if(!formatUtil.checkNull(newMail,MailCode))
             return Result.error(StatusCode.ERROR,"参数错误");
         if(!formatUtil.isMail(newMail))
             return Result.error(StatusCode.ERROR,"邮箱格式错误");
         try{
             userservice.updateMailSendState(newMail,MailCode);
             return Result.success("发送成功");
         }
         catch (RuntimeException e){
             return Result.error(StatusCode.ERROR,e.getMessage());
         }
    }
    @RequireRole("USER")
    @PostMapping("/forgetPassword")
    public Result forgetPassword(String userName, String mailCode, String newPassword) {
         if(!formatUtil.checkNull(userName, mailCode, newPassword))
             return Result.error(StatusCode.ERROR,"参数错误");
         try{
             userservice.forgetPassword(userName, mailCode, newPassword);
             return Result.success("重置成功");
         }catch (RuntimeException e){
             return Result.error(StatusCode.ERROR,e.getMessage());
         }
    }

    @RequireRole("ADMIN")
    @GetMapping("/{page}/{showCount}")
    public Result findUser(@PathVariable Integer page, @PathVariable Integer showCount){
         if(!formatUtil.checkPositive(page, showCount))
             return Result.error(StatusCode.ERROR,"参数错误");
        PageResult<User> pageResult=userservice.findUserPage(page, showCount);
        return Result.success("查询成功",pageResult);

    }

    @RequireRole("ADMIN")
    @GetMapping("/search/{page}/{showCount}")
public Result searchUser(String userName, @PathVariable Integer page, @PathVariable Integer showCount)
{
       if(!formatUtil.checkNull(userName))
           return Result.error(StatusCode.ERROR,"参数错误");
       if(!formatUtil.checkPositive(page, showCount))
           return Result.error(StatusCode.ERROR,"参数错误");
       PageResult<User> pageResult=userservice.searchUserByName(userName,page, showCount);
       return Result.success("查询成功",pageResult);
}

    @RequireRole("USER")
    @PutMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam String avatarPath){
        if(!formatUtil.checkNull(avatarPath))
            return Result.error(StatusCode.ERROR,"参数错误");
        userservice.updateAvatar(avatarPath);
        return Result.success("更新头像成功");
    }


}

