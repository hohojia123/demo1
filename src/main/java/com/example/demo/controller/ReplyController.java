package com.example.demo.controller;


import com.example.demo.common.RequireRole;
import com.example.demo.model.entity.Result;
import com.example.demo.model.entity.StatusCode;
import com.example.demo.model.pojo.Reply;
import com.example.demo.service.ReplyService;
import com.example.demo.uitl.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reply")
public class ReplyController {
    @Autowired
    private ReplyService replyService;
    @Autowired
    private FormatUtil formatUtil;


    @RequireRole("user")
    @PostMapping("/{discussId}")
    public Result reply(@PathVariable Integer discussId, String replyBody, Integer rootId){
        if(!formatUtil.checkNull(replyBody))
            return Result.error(StatusCode.ERROR,"参数异常");
        if(!formatUtil.checkPositive(discussId))
            return Result.error(StatusCode.ERROR,"参数异常");
        try{
            replyService.saveReply(discussId,replyBody,rootId);
            return Result.success("回复成功");
        }catch(RuntimeException e){
            return Result.error(StatusCode.ERROR,"回复失败"+e.getMessage());
        }
     }


    @RequireRole("user")
    @DeleteMapping("/{replyId}")
    public  Result deleteReply(@PathVariable Integer replyId){
        if(!formatUtil.checkPositive(replyId))
            return Result.error(StatusCode.ERROR,"参数异常");
        try{
            replyService.deleteReply(replyId);
            return Result.success("删除成功");
        }catch(RuntimeException e){
            return Result.error(StatusCode.ERROR,"删除失败"+e.getMessage());
        }
    }
    @RequireRole("admin")
    @DeleteMapping("/admin/{replyId}")
    public  Result adminDeleteReply(@PathVariable Integer replyId){
        if(!formatUtil.checkPositive(replyId))
            return Result.error(StatusCode.ERROR,"参数异常");
        try{
            replyService.adminDeleteReply(replyId);
            return Result.success("删除成功");
        }catch(RuntimeException e){
            return Result.error(StatusCode.ERROR,"删除失败"+e.getMessage());
        }
    }
}
