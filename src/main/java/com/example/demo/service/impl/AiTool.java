package com.example.demo.service.impl;


import cn.hutool.json.JSONUtil;
import com.example.demo.common.BaseContext;
import com.example.demo.common.RequireRole;
import com.example.demo.mapper.BolgMapper;
import com.example.demo.mapper.DiscussMapper;
import com.example.demo.mapper.TagMapper;
import com.example.demo.mapper.userMapper;
import com.example.demo.model.pojo.Blog;
import com.example.demo.model.pojo.Discuss;
import com.example.demo.model.pojo.KnowledgeChunk;
import com.example.demo.model.pojo.User;
import com.example.demo.service.BlogService;
import com.example.demo.service.impl.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class AiTool {

    private BolgMapper bolgMapper;

    private BlogService blogService;


    private DiscussMapper discussMapper;

    private TagMapper tagMapper;

    private userMapper userMapper;

    private RagService ragService;

    public  AiTool(BolgMapper bolgMapper, BlogService blogService,
                   DiscussMapper discussMapper,
                   TagMapper tagMapper,
                   userMapper userMapper,
                   RagService ragService) {
        this.bolgMapper = bolgMapper;
        this.blogService = blogService;
        this.discussMapper = discussMapper;
        this.tagMapper = tagMapper;
        this.userMapper = userMapper;
        this.ragService = ragService;
    }

    @RequireRole("user")
    @Tool(name= "search_blog",description = "根据关键字搜索博客信息")
    public String search_blog(
            @ToolParam(description = "搜索关键字内容")
            String searchText){
         List<Blog>blogs= bolgMapper.searchBlog(searchText);
         String res= JSONUtil.toJsonStr(blogs);
         log.info("执行search_blog");
         return res;
    }

    @RequireRole("user")
    @Tool(description = "根据博客ID查询博客信息",name = "get_blogByID")
    public  String get_blogByID(@ToolParam(description = "查询的博客ID")Integer blogId){
           Blog blog= bolgMapper.selectById(blogId);
           log.info("执行get_blogByID");
           return JSONUtil.toJsonStr(blog);
    }

    @Tool(description = "获取当前热门博客信息",name = "get_hotblog")
    public  String get_hotblog() throws IOException {
        List<Blog>blogs= blogService.findHotBlog();
        log.info("执行get_hotblog");
        return JSONUtil.toJsonStr(blogs);
    }

    @RequireRole("user")
    @Tool(description = "根据当前用户id获取当前用户博客信息",name = "get_my_blog")
    public String get_my_blog(){
        Integer userId= BaseContext.getCurrentId().intValue();
        List<Blog>blogs= bolgMapper.selectByUserId(userId);
        log.info("执行get_my_blog");
        return JSONUtil.toJsonStr(blogs);
    }

    @Tool(description = "获取首页博客信息",name="get_homeblog")
    public  String get_homeblog(@ToolParam(description = "当前页码",required = true)Integer page,
                                @ToolParam(description = "每页显示数量",required = true)Integer showCount) throws IOException {
        return JSONUtil.toJsonStr(blogService.findHomeBlog(page,showCount));
    }


    @RequireRole("user")
    @Tool(description = "获取当前用户的所有评论信息",name="get_blogComment")
    public  String get_blogComment(){
        Integer userId= BaseContext.getCurrentId().intValue();
        return JSONUtil.toJsonStr(discussMapper.selectById(userId));
    }

    @Tool(description = "获取全站前若干条最近的评论信息",name="get_RecentComments")
    public String get_RecentComments(@ToolParam(description = "获取最近评论数量",required = true)Integer  count){
        Integer userId= BaseContext.getCurrentId().intValue();
        return JSONUtil.toJsonStr(discussMapper.findNewDiscuss(count));
    }

    @Tool(name = "getBlogLikes", description = "获取指定博客的点赞数量")
    public String getBlogLikes(
            @ToolParam(description = "博客ID") Integer blogId) {
        return "博客id为"+blogId.toString()+"的点赞数量为"+JSONUtil.toJsonStr(blogService.getBlogLikeCountByBlogId(blogId));
    }

    @RequireRole("user")
    @Tool(name = "getMyTags", description = "获取当前用户所有标签")
    public String getMyTags(){
        Integer userId=BaseContext.getCurrentId().intValue();
        return JSONUtil.toJsonStr(tagMapper.findTagByUserId(userId));
    }

    @RequireRole("user")
    @Tool(name = "getMyNotifications", description = "获取博客收到的评论通知")
    public String getMyNotifications(@ToolParam(description = "最新评论前n条")Integer  count) {
        return JSONUtil.toJsonStr(discussMapper.findUserNewDiscuss(BaseContext.getCurrentId().intValue(),count));
    }


    @RequireRole("user")
    @Tool(name = "getMyEmail", description = "根据用户id获取当前登录用户的信息，密码注空")
    public String getMyEmail() {
        Integer userId= BaseContext.getCurrentId().intValue();
        User user= userMapper.selectById(userId);
        user.setPassword( null);
        return JSONUtil.toJsonStr(user);
    }


    @RequireRole("admin")
    @Tool(name = "getAllBlog", description = "获取全站所有博客包含已经删除的")
    public String getAllBlog(){
        return JSONUtil.toJsonStr(bolgMapper.findAllBlog());
    }

    @RequireRole("admin")
    @Tool(name = "searchAllBlog", description = "获取全站所有博客包含已经删除的")
    public String searchAllBlog(@ToolParam(description = "搜索关键字")String searchText){
        return JSONUtil.toJsonStr(bolgMapper.searchAllBlog(searchText));
    }

    @RequireRole("admin")
    @Tool(name = "getAllUsers", description = "获取全站所有用户包含禁用状态用户")
    public String getAllUsers(){
        return JSONUtil.toJsonStr(userMapper.findUserPage());
    }

    @RequireRole("admin")
    @Tool(name = "searchUser", description = "根据用户名获取全站所有用户包含禁用状态用户")
    public String searchUser(@ToolParam(description = "用户名关键字")String searchText){
        return JSONUtil.toJsonStr(userMapper.searchUserByName(searchText));
    }

    @Tool(name = "searchRag", description = "在博客知识库中搜索相关信息，当用户询问博客具体内容时优先使用此工具")
    public String searchRag(
            @ToolParam(description = "搜索查询内容") String query,
            @ToolParam(description = "返回结果数量（默认5）") Integer topK) {
        if (topK == null || topK < 1) topK = 5;
        List<KnowledgeChunk> chunks = ragService.search(query, topK);
        StringBuilder sb = new StringBuilder();
        for (KnowledgeChunk chunk : chunks) {
            sb.append("【相关片段】").append(chunk.getContent()).append("\n\n");
        }
        log.info("执行searchRag，查询={}，返回{}条结果", query, chunks.size());
        return sb.toString();
    }






}
