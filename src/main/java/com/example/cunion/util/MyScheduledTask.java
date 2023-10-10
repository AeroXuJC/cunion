package com.example.cunion.util;

import com.example.cunion.service.ShopService;
import com.example.cunion.service.UserService;
import lombok.Data;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Data
@Component
public class MyScheduledTask {


    @Resource
    private UserService userService;

    @Resource
    private ShopService shopService;

    // 每隔5分钟执行一次任务
    @Scheduled(fixedRate = 1000 * 60 * 60)
    public void myTask() {
        Integer resultUserThumb = userService.syncUserThumbRedisAndDb();
        Integer resultUserCollect = userService.syncUserCollectRedisAndDb();
        Integer resultShopThumb = shopService.syncShopThumbRedisAndDb();
        Integer resultShopCollect = shopService.syncShopCollectRedisAndDb();
        // 这里编写你的定时任务逻辑
        System.out.println("定时任务执行了！");
    }
}