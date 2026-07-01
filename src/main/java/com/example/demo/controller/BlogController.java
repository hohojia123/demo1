package com.example.demo.controller;


import com.example.demo.common.RequireRole;
import com.example.demo.configuration.RedisConfig;
import com.example.demo.model.entity.PageResult;
import com.example.demo.model.entity.Result;
import com.example.demo.model.entity.StatusCode;
import com.example.demo.model.pojo.Blog;
import com.example.demo.service.BlogService;
import com.example.demo.uitl.FormatUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/blog")
public class BlogController {
    @Autowired
    private BlogService blogService;
    @Autowired
    private FormatUtil formatUtil;
    @Autowired
    private RedisConfig redisConfig;
    private static final String IMAGE_JPG = ".jpg";

    private static final String IMAGE_PNG = ".png";

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @GetMapping("/test")
    public void test(){
        kafkaTemplate.send("order-topic","test","hello");
    }
    @RequireRole("USER")
    @PostMapping("/uploadImg")
    public Result uploadImg(MultipartFile file){
        if(!formatUtil.checkObjectNull(file))
            return Result.error(StatusCode.ERROR,"参数错误");
        String fileFormat=formatUtil.getFileformat(file.getOriginalFilename());
        if(fileFormat==null)
            return Result.error(StatusCode.ERROR,"图片缺少格式");
        if(!IMAGE_JPG.equals(fileFormat.toLowerCase()) && !IMAGE_PNG.equals(fileFormat.toLowerCase()))
            return Result.error(StatusCode.ERROR,"图片格式错误");
        try{
            return Result.success("上传成功",blogService.saveImg(file));
        }catch (Exception e){
            return Result.error(StatusCode.ERROR,"上传失败" + e.getMessage());
        }
    }

    @RequireRole("USER")
@PostMapping
public  Result newBlog(String blogTitle, String blogBody, Integer[] tagIds){
        if(!formatUtil.checkNull(blogTitle, blogBody) || !formatUtil.checkPositive(tagIds))
            return Result.error(StatusCode.ERROR,"参数错误");
        try{
            blogService.saveBlog(blogTitle, blogBody, tagIds);
            return Result.success("发布成功");
        }catch(IOException e){
            return Result.error(StatusCode.ERROR,"非法操作" + e.getMessage());
        }
}

    @GetMapping("/{blogId}/{isHistory}")
    public Result findBlogById(@PathVariable Integer blogId, @PathVariable boolean isHistory) {
        if (!formatUtil.checkObjectNull(blogId, isHistory)) {
            return Result.error(StatusCode.ERROR, "参数错误");
        }
        try {
            return Result.success( "查询成功", blogService.findBlogById(blogId, isHistory));
        } catch (RuntimeException e) {
            System.out.println("=== 博客不存在异常: " + e.getMessage());
            return Result.error(StatusCode.NOTFOUND, "此博客不存在");
        } catch (IOException e) {
            System.out.println("=== IO异常: " + e.getMessage());
            return Result.error(StatusCode.ERROR, "此博客不存在");
        }
    }

    @RequireRole("USER")
    @GetMapping("/myblog/{page}/{showCount}")
    public Result findBlogByUser(@PathVariable Integer page, @PathVariable Integer showCount) {

        if (!formatUtil.checkPositive(page, showCount)) {
            return Result.error(StatusCode.ERROR, "参数错误");
        }
        PageResult<Blog> pageResult =
                new PageResult<>(blogService.findBlogByUser(page, showCount));
        return Result.success( "查询成功", pageResult);
    }

    @GetMapping("/home/{page}/{showCount}")
    public Result homeBlog(@PathVariable Integer page, @PathVariable Integer showCount) {
        if (!formatUtil.checkPositive(page, showCount) || showCount > RedisConfig.REDIS_NEW_BLOG_COUNT) {
            return Result.error(StatusCode.ERROR, "参数错误");
        }
        try {
            PageResult<Blog> pageResult = new PageResult<>( blogService.findHomeBlog(page, showCount));
            return Result.success("查询成功", pageResult);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(StatusCode.SERVICEERROR, "服务异常");
        }
    }

    @GetMapping("/hotBlog")
    public Result hotBlog() {
        try {
            return Result.success( "查询成功", blogService.findHotBlog());
        } catch (IOException e) {
            return Result.error(StatusCode.SERVICEERROR, "服务异常");
        }
    }

    @GetMapping("/searchBlog/{page}/{showCount}")
    public Result searchBlog(String search,
                             @PathVariable Integer page,
                             @PathVariable Integer showCount) {
        if (!formatUtil.checkPositive(page, showCount) || showCount > RedisConfig.REDIS_NEW_BLOG_COUNT) {
            return Result.error(StatusCode.ERROR, "参数错误");
        }
        if (!formatUtil.checkNull(search)) {
            return Result.error(StatusCode.ERROR, "参数错误");
        }
        PageResult<Blog> pageResult = new PageResult<>(
                blogService.searchBlog(search, page, showCount));
        return Result.success( "查询成功", pageResult);
    }


    @RequireRole("ADMIN")
    @GetMapping("/AllBlog/{page}/{showCount}")
    public Result findAllBlog(@PathVariable Integer page, @PathVariable Integer showCount) {
        if (!formatUtil.checkPositive(page, showCount)) {
            return Result.error(StatusCode.ERROR, "参数错误");
        }
        PageResult<Blog> pageResult = new PageResult<>( blogService.findAllBlog(page, showCount));

        return Result.success( "查询成功", pageResult);
    }


    @RequireRole("USER")
    @PutMapping("/{blogId}")
    public Result updateBlog(@PathVariable Integer blogId, String blogTitle, String blogBody, Integer[] tagId) {

        if (!formatUtil.checkNull(blogTitle, blogBody)) {
            return Result.error(StatusCode.ERROR, "参数错误");
        }

        if (!formatUtil.checkPositive(tagId) || !formatUtil.checkPositive(blogId)) {
            return Result.error(StatusCode.ERROR, "参数错误");
        }

        try {
            blogService.updateBlog(blogId, blogTitle, blogBody, tagId);
            return Result.success( "修改成功");
        } catch (RuntimeException e) {
            return Result.error(StatusCode.ERROR, "修改失败" + e.getMessage());
        } catch (IOException e) {
            return Result.error(StatusCode.SERVICEERROR, "服务异常");
        }
    }
    @RequireRole("USER")
    @DeleteMapping("/{blogId}")
    public Result deleteBlog(@PathVariable Integer blogId) {
        if (!formatUtil.checkPositive(blogId)) {
            return Result.error(StatusCode.ERROR, "参数错误");
        }
        try {
            blogService.deleteBlog(blogId);
            return Result.error(StatusCode.OK, "删除成功");
        } catch (RuntimeException e) {
            return Result.error(StatusCode.ERROR, "删除失败" + e.getMessage());
        } catch (JsonProcessingException e) {
            return Result.error(StatusCode.SERVICEERROR, "服务异常");
        }
    }
    @RequireRole("ADMIN")
    @DeleteMapping("/admin/{blogId}")
    public Result adminDeleteBlog(@PathVariable Integer blogId) throws JsonProcessingException {
        if (!formatUtil.checkPositive(blogId)) {
            return Result.error(StatusCode.ERROR, "参数错误");
        }
        blogService.adminDeleteBlog(blogId);
        return Result.success("删除成功");
    }

    @RequireRole("ADMIN")
    @GetMapping("/searchAllBlog/{page}/{showCount}")
    public Result searchAllBlog(String search,
                                @PathVariable Integer page,
                                @PathVariable Integer showCount) {
        if (!formatUtil.checkPositive(page, showCount)) {
            return Result.error(StatusCode.ERROR, "参数错误");
        }
        if (!formatUtil.checkNull(search)) {
            return Result.error(StatusCode.ERROR, "参数错误");
        }
        PageResult<Blog> pageResult = new PageResult<>(
                blogService.searchAllBlog(search, page, showCount));
        return Result.success( "查询成功", pageResult);
    }


    @GetMapping("/statisticalBlogByMonth")
    public Result statisticalBlogByMonth() {
        try {
            return Result.success( "查询成功", blogService.statisticalBlogByMonth());
        } catch (IOException e) {
            return Result.error(StatusCode.SERVICEERROR, "服务异常");
        }
    }

    @GetMapping("/getBlogLikeCount/{blogId}")
    public Result getBlogLikeCount(@PathVariable Integer blogId) {
        try {
            int likeCount = blogService.getBlogLikeCountByBlogId(blogId);
            return Result.success( "获取点赞数成功", likeCount);
        } catch (RuntimeException re) {
            return Result.error(StatusCode.ERROR, re.getMessage());
        }
    }
}
