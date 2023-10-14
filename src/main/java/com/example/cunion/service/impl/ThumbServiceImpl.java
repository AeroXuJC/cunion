package com.example.cunion.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.cunion.exception.CunionException;
import com.example.cunion.mapper.ShopMapper;
import com.example.cunion.service.ThumbService;
import com.example.cunion.util.MyScheduledTask;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ThumbServiceImpl implements ThumbService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ShopMapper shopMapper;

    @Resource
    private MyScheduledTask myScheduledTask;

    @Override
    public void addThumb(String shopId, String userId) {
        // 根据shopId查询商家信息
        HashMap map = shopMapper.searchShopById(shopId);
        // 如果商家不存在，抛出异常
        if(map == null){
            throw new CunionException("该商家不存在！");
        }
        // 检查是否已经点赞
        String shopIsExist = stringRedisTemplate.opsForValue().get("shopThumbIsExist:" + userId + ":" + shopId);
        // 如果已经点赞，抛出异常
        if(shopIsExist != null){
            throw new CunionException("不能重复点赞！");
        }
        // 从user:thumb中取出一个
        String leftPopUser = stringRedisTemplate.opsForList().leftPop("user:thumb:" + userId);
        // 如果user:thumb为空，则新建一个
        if(leftPopUser == null || leftPopUser.equals("[]")){
            ArrayList arrayList = new ArrayList();
            arrayList.add(shopId);
            stringRedisTemplate.opsForList().leftPush("user:thumb:" + userId, arrayList.toString());
        }else {
            // 将leftPopUser转换为List
            List<String> list = JSONUtil.toList(leftPopUser, String.class);
            list.add(shopId);
            stringRedisTemplate.opsForList().leftPush("user:thumb:" + userId, list.toString());
        }
        // 从shop:thumb中取出一个
        String leftPop = stringRedisTemplate.opsForList().leftPop("shop:thumb:" + shopId);
        // 如果shop:thumb为空，则新建一个
        if(leftPop == null || leftPop.equals("[]")){
            ArrayList arrayList = new ArrayList();
            arrayList.add(userId);
            stringRedisTemplate.opsForList().leftPush("shop:thumb:" + shopId, arrayList.toString());
            stringRedisTemplate.opsForValue().set("shopThumbIsExist:" + userId + ":" + shopId, userId + shopId);
        }else {
            /**
             * 去除[]中的""
             */
            // 将leftPop转换为List
            List<String> jsonArray = JSONUtil.toList(leftPop, String.class);

            jsonArray.add(userId);
            stringRedisTemplate.opsForList().leftPush("shop:thumb:" + shopId, jsonArray.toString());
            stringRedisTemplate.opsForValue().set("shopThumbIsExist:" + userId + ":" + shopId, userId + shopId);
        }
        // 执行定时任务
        myScheduledTask.myTask();
    }

    @Override
    public void removeThumb(String shopId, String userId) {
        // 根据shopId查询商家信息
        HashMap map = shopMapper.searchShopById(shopId);
        // 如果商家不存在，抛出异常
        if(map == null){
            throw new CunionException("该商家不存在！");
        }
        // 检查是否已经点赞
        String isExist = stringRedisTemplate.opsForValue().get("shopThumbIsExist:" + userId + ":" + shopId);
        // 如果还没有点赞，抛出异常
        if(isExist == null || isExist.equals("[]")){
            throw new CunionException("还未点赞！");
        }
        // 删除点赞
        stringRedisTemplate.delete("shopThumbIsExist:" + userId + ":" + shopId);
        // 从shop:thumb中取出一个
        String leftPop = stringRedisTemplate.opsForList().leftPop("shop:thumb:" + shopId);
        // 从user:thumb中取出一个
        String leftPopUser = stringRedisTemplate.opsForList().leftPop("user:thumb:" + userId);
        // 将leftPop转换为List
        List<String> list = JSONUtil.toList(leftPop, String.class);
        // 将leftPopUser转换为List
        List<String> listUser = JSONUtil.toList(leftPopUser, String.class);
        // 移除userId
        list.remove(userId);
        // 移除shopId
        listUser.remove(shopId);
        // 将list添加到shop:thumb中
        stringRedisTemplate.opsForList().leftPush("shop:thumb:" + shopId, list.toString());
        // 将listUser添加到user:thumb中
        stringRedisTemplate.opsForList().leftPush("user:thumb:" + userId, listUser.toString());
        // 执行定时任务
        myScheduledTask.myTask();
    }
}
