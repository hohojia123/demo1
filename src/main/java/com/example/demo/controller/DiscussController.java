package com.example.demo.controller;

import com.example.demo.common.RequireRole;
import com.example.demo.model.entity.PageResult;
import com.example.demo.model.entity.Result;
import com.example.demo.model.entity.StatusCode;
import com.example.demo.model.pojo.Discuss;
import com.example.demo.service.DiscussService;
import com.example.demo.uitl.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/discuss")
public class DiscussController {

    @Autowired
    private DiscussService discussService;
    @Autowired
    private FormatUtil formatUtil;


     @PostMapping("/{bolgId}")
     @RequireRole("user")
    public Result discuss(String discussBody, @PathVariable Integer bolgId){
        if(!formatUtil.checkNull(discussBody))
            return Result.error(StatusCode.ERROR,"参数错误");
       if(!formatUtil.checkPositive(bolgId))
           return Result.error(StatusCode.ERROR,"参数错误");
       discussService.saveDiscuss(discussBody,bolgId);
       return Result.success("评论成功");
    }


     @RequireRole("USER")
    @DeleteMapping("/{discussId}")
    public Result deleteDiscuss(@PathVariable Integer discussId){
        if(!formatUtil.checkPositive(discussId))
            return Result.error(StatusCode.ERROR,"参数错误");
        try{
            discussService.deleteDiscuss(discussId);
            return Result.success("删除评论成功");
        }catch(RuntimeException e){
            return Result.error(StatusCode.ERROR,"删除失败"+e.getMessage());
         }
     }

    @RequireRole("ADMIN")
    @DeleteMapping("/admin/{discussId}")
    public Result adminDeleteDiscuss(@PathVariable Integer discussId) {
        if (!formatUtil.checkPositive(discussId)) {
            return Result.error(StatusCode.ERROR,"参数错误");
        }
        try {
            discussService.adminDeleteDiscuss(discussId);
            return Result.success( "删除评论成功");
        } catch (RuntimeException e) {
            return Result.error(StatusCode.ERROR, "删除失败" + e.getMessage());
        }

    }


    @GetMapping("/{blogId}/{page}/{showCount}")
    public Result getDiscussByBlog(@PathVariable Integer blogId,
                                   @PathVariable Integer page,
                                   @PathVariable Integer showCount) {
           if(!formatUtil.checkPositive(blogId,page,showCount))
               return Result.error(StatusCode.ERROR,"参数错误");
        PageResult<Discuss> pageResult=new PageResult<>(discussService.findDiscussByBlogId(blogId,page,showCount));
             return Result.success("查询成功",pageResult);
    }

    @GetMapping("/newDiscuss")
    public Result newDiscuss(){
        return Result.success("查询成功",discussService.findNewDiscuss());
    }

    @RequireRole("USER")
    @GetMapping("/userNewDiscuss")
    public Result userNewDiscuss(){
        return Result.success("查询成功",discussService.findUserNewDiscuss());
    }

}
