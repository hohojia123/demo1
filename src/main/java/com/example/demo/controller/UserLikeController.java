package com.example.demo.controller;


import com.example.demo.common.RequireRole;
import com.example.demo.model.entity.Result;
import com.example.demo.model.entity.StatusCode;
import com.example.demo.model.pojo.UserLike;
import com.example.demo.service.UserLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/userLike")
public class UserLikeController {
    @Autowired
    private UserLikeService userLikeService;

    @PostMapping("/saveUserLike")
    @RequireRole("USER")
  public Result saveUserLike(@RequestParam Integer blogId, @RequestParam Integer status){
        boolean isLiked = userLikeService.getUserLike(blogId);
        Integer newStatus = status;                // 1=想点赞, 0=想取消
        if (newStatus == 1 && isLiked) {
            return Result.error(StatusCode.ERROR, "你已点过赞");
        }
        if (newStatus == 0 && !isLiked) {
            return Result.error(StatusCode.ERROR, "你还未点赞");
        }
        try {
            UserLike userLike = new UserLike();
            com.example.demo.model.pojo.Blog blog = new com.example.demo.model.pojo.Blog();
            blog.setId(blogId);
            userLike.setBlog(blog);
            userLike.setStatus(status);
            userLikeService.saveUserLike(userLike);
            String msg = newStatus == 1 ? "点赞成功" : "取消点赞成功";
            return Result.success( msg);
        } catch (RuntimeException re) {
            return Result.error(StatusCode.ERROR, re.getMessage());
        }
  }

  @GetMapping("/isUserLike/{blogId}")
  @RequireRole("USER")
   public Result getUserLike(@PathVariable Integer blogId){
      try {
          return Result.success( "获取点赞记录成功", userLikeService.getUserLike(blogId));
      } catch (RuntimeException re) {
          return Result.error(StatusCode.ERROR, re.getMessage());
      }

   }
}
