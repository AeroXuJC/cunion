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
        HashMap map = shopMapper.searchShopById(shopId);
        if(map == null){
            throw new CunionException("该商家不存在！");
        }
        String shopIsExist = stringRedisTemplate.opsForValue().get("shopThumbIsExist:" + userId + ":" + shopId);
        if(shopIsExist != null){
            throw new CunionException("不能重复点赞！");
        }
        String leftPopUser = stringRedisTemplate.opsForList().leftPop("user:thumb:" + userId);
        if(leftPopUser == null || leftPopUser.equals("[]")){
            ArrayList arrayList = new ArrayList();
            arrayList.add(shopId);
            stringRedisTemplate.opsForList().leftPush("user:thumb:" + userId, arrayList.toString());
        }else {
            List<String> list = JSONUtil.toList(leftPopUser, String.class);
            list.add(shopId);
            stringRedisTemplate.opsForList().leftPush("user:thumb:" + userId, list.toString());
        }
        String leftPop = stringRedisTemplate.opsForList().leftPop("shop:thumb:" + shopId);
        if(leftPop == null || leftPop.equals("[]")){
            ArrayList arrayList = new ArrayList();
            arrayList.add(userId);
            stringRedisTemplate.opsForList().leftPush("shop:thumb:" + shopId, arrayList.toString());
            stringRedisTemplate.opsForValue().set("shopThumbIsExist:" + userId + ":" + shopId, userId + shopId);
        }else {
            /**
             * 去除[]中的""
             */
            List<String> jsonArray = JSONUtil.toList(leftPop, String.class);

            jsonArray.add(userId);
            stringRedisTemplate.opsForList().leftPush("shop:thumb:" + shopId, jsonArray.toString());
            stringRedisTemplate.opsForValue().set("shopThumbIsExist:" + userId + ":" + shopId, userId + shopId);
        }
        myScheduledTask.myTask();
    }

    @Override
    public void removeThumb(String shopId, String userId) {
        HashMap map = shopMapper.searchShopById(shopId);
        if(map == null){
            throw new CunionException("该商家不存在！");
        }
        String isExist = stringRedisTemplate.opsForValue().get("shopThumbIsExist:" + userId + ":" + shopId);
        if(isExist == null || isExist.equals("[]")){
            throw new CunionException("还未点赞！");
        }
        stringRedisTemplate.delete("shopThumbIsExist:" + userId + ":" + shopId);
        String leftPop = stringRedisTemplate.opsForList().leftPop("shop:thumb:" + shopId);
        String leftPopUser = stringRedisTemplate.opsForList().leftPop("user:thumb:" + userId);
        List<String> list = JSONUtil.toList(leftPop, String.class);
        List<String> listUser = JSONUtil.toList(leftPopUser, String.class);
        list.remove(userId);
        listUser.remove(shopId);
        stringRedisTemplate.opsForList().leftPush("shop:thumb:" + shopId, list.toString());
        stringRedisTemplate.opsForList().leftPush("user:thumb:" + userId, listUser.toString());
        myScheduledTask.myTask();
    }
}
