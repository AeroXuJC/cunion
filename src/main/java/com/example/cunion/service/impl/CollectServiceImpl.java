package com.example.cunion.service.impl;

import cn.hutool.json.JSONUtil;
import com.example.cunion.exception.CunionException;
import com.example.cunion.service.CollectService;
import com.example.cunion.util.MyScheduledTask;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class CollectServiceImpl implements CollectService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MyScheduledTask myScheduledTask;

    @Override
    public void addCollect(String shopId, String userId) {
        String isExist = stringRedisTemplate.opsForValue().get("collectIsExist:" + userId + ":" + shopId);
        if(isExist != null){
            throw new CunionException("不能重复收藏！");
        }
        String leftPopUser = stringRedisTemplate.opsForList().leftPop("user:collect:" + userId);
        if(leftPopUser == null || leftPopUser.equals("[]")){
            ArrayList arrayList = new ArrayList();
            arrayList.add(shopId);
            stringRedisTemplate.opsForList().leftPush("user:collect:" + userId, arrayList.toString());
        }else {
            List<String> list = JSONUtil.toList(leftPopUser, String.class);
            list.add(shopId);
            stringRedisTemplate.opsForList().leftPush("user:collect:" + userId, list.toString());
        }
        String leftPop = stringRedisTemplate.opsForList().leftPop("shop:collect:" + shopId);
        if(leftPop == null || leftPop.equals("[]")){
            ArrayList arrayList = new ArrayList();
            arrayList.add(userId);
            stringRedisTemplate.opsForList().leftPush("shop:collect:" + shopId, arrayList.toString());
            stringRedisTemplate.opsForValue().set("collectIsExist:" + userId + ":" + shopId, userId + shopId);
        }else {
            /**
             * 去除[]中的""
             */
            List<String> jsonArray = JSONUtil.toList(leftPop, String.class);

            jsonArray.add(userId);
            stringRedisTemplate.opsForList().leftPush("shop:collect:" + shopId, jsonArray.toString());
            stringRedisTemplate.opsForValue().set("collectIsExist:" + userId + ":" + shopId, userId + shopId);
        }
        myScheduledTask.myTask();
    }

    @Override
    public void removeCollect(String shopId, String userId) {
        String isExist = stringRedisTemplate.opsForValue().get("collectIsExist:" + userId + ":" + shopId);
        if(isExist == null){
            throw new CunionException("还未收藏！");
        }
        stringRedisTemplate.delete("collectIsExist:" + userId + ":" + shopId);
        String leftPopUser = stringRedisTemplate.opsForList().leftPop("user:collect:" + userId);
        String leftPop = stringRedisTemplate.opsForList().leftPop("shop:collect:" + shopId);
        List<String> listUser = JSONUtil.toList(leftPopUser, String.class);
        List<String> list = JSONUtil.toList(leftPop, String.class);
        listUser.remove(shopId);
        list.remove(userId);
        stringRedisTemplate.opsForList().leftPush("user:collect:" + userId, listUser.toString());
        stringRedisTemplate.opsForList().leftPush("shop:collect:" + shopId, list.toString());
        myScheduledTask.myTask();
    }
}
