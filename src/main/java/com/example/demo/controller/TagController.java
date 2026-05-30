package com.example.demo.controller;


import com.example.demo.common.RequireRole;
import com.example.demo.model.entity.Result;
import com.example.demo.service.TagService;
import com.example.demo.uitl.FormatUtil;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tag")
public class TagController {

    @Autowired
    private TagService tagService;
    @Autowired
    private FormatUtil formatUtil;



    @PostMapping
    @RequireRole("user")
    public Result newTag(String tagName){
        if(!formatUtil.checkNull(tagName))
            return Result.error("参数异常");
        try{
            tagService.saveTag(tagName);
            return Result.success("新增成功");
        }catch (RuntimeException e){
            return Result.error("新增失败,"+e.getMessage());
        }
    }


    @RequireRole("user")
    @DeleteMapping("/{tagId}")
    public Result deleteTag(@PathVariable Integer tagId){
        if(!formatUtil.checkObjectNull(tagId))
            return Result.error("参数异常");
        try{
            tagService.deleteTagById(tagId);
            return Result.success("删除成功");
        }catch (RuntimeException e){
            return Result.error("删除失败,"+e.getMessage());
        }
    }



    @RequireRole("user")
    @PutMapping
    public Result updateTag(Integer tagId, String tagName) {
        if (!formatUtil.checkObjectNull(tagId))
            return Result.error("参数异常");
        if (!formatUtil.checkNull(tagName))
            return Result.error("参数异常");
        try {
            tagService.updateTag(tagId, tagName);
            return Result.success("修改成功");
        } catch (RuntimeException e) {
            return Result.error("修改失败,"+e.getMessage());
        }

    }
    @RequireRole("user")
    @GetMapping
    public Result findTagByUserId() {
        return Result.success( "查询成功", tagService.findTagByUserId());
    }


}
