package com.example.demo.task;


import com.example.demo.service.BlogService;
import com.example.demo.service.UserLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@EnableScheduling
public class UserLikeTask {
    @Autowired
    private BlogService blogService;

    @Autowired
    private UserLikeService userLikeService;
    /**
     * @Description: 5小时执行一次
     * @Param: []
     * @return: void
     * @Author: Tyson
     * @Date: 2020/5/30/0030 14:51
     */
    @Scheduled(fixedRate = 1000 * 60 * 5)
    private void userLikeTask() {
        userLikeService.UserLikeFromRedistoDB();
        blogService.transLikeCountFromRedis2DB();
    }

}
