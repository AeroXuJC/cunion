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
        //判断是否已经收藏
        String isExist = stringRedisTemplate.opsForValue().get("collectIsExist:" + userId + ":" + shopId);
        if(isExist != null){
            throw new CunionException("不能重复收藏！");
        }
        //从用户收藏列表中取出一个
        String leftPopUser = stringRedisTemplate.opsForList().leftPop("user:collect:" + userId);
        if(leftPopUser == null || leftPopUser.equals("[]")){
            ArrayList arrayList = new ArrayList();
            arrayList.add(shopId);
            stringRedisTemplate.opsForList().leftPush("user:collect:" + userId, arrayList.toString());
        }else {
            //将取出的字符串转换成list
            List<String> list = JSONUtil.toList(leftPopUser, String.class);
            list.add(shopId);
            stringRedisTemplate.opsForList().leftPush("user:collect:" + userId, list.toString());
        }
        //判断商品收藏列表中是否有该用户
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
        //执行定时任务
        myScheduledTask.myTask();
    }

    @Override
    public void removeCollect(String shopId, String userId) {
        //从Redis中获取是否存在的标识
        String isExist = stringRedisTemplate.opsForValue().get("collectIsExist:" + userId + ":" + shopId);
        //如果标识为空，则抛出异常
        if(isExist == null){
            throw new CunionException("还未收藏！");
        }
        //从Redis中删除标识
        stringRedisTemplate.delete("collectIsExist:" + userId + ":" + shopId);
        //从Redis中获取用户收藏列表
        String leftPopUser = stringRedisTemplate.opsForList().leftPop("user:collect:" + userId);
        //从Redis中获取商品收藏列表
        String leftPop = stringRedisTemplate.opsForList().leftPop("shop:collect:" + shopId);
        //将获取的列表转换为List集合
        List<String> listUser = JSONUtil.toList(leftPopUser, String.class);
        List<String> list = JSONUtil.toList(leftPop, String.class);
        //从用户收藏列表中移除商品
        listUser.remove(shopId);
        //从商品收藏列表中移除用户
        list.remove(userId);
        //将更新后的用户收藏列表重新放入Redis
        stringRedisTemplate.opsForList().leftPush("user:collect:" + userId, listUser.toString());
        //将更新后的商品收藏列表重新放入Redis
        stringRedisTemplate.opsForList().leftPush("shop:collect:" + shopId, list.toString());
        //执行定时任务
        myScheduledTask.myTask();
    }
}
