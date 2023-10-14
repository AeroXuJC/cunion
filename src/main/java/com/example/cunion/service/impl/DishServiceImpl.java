package com.example.cunion.service.impl;

import com.example.cunion.mapper.DishMapper;
import com.example.cunion.service.DishService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DishServiceImpl implements DishService {

    @Resource
    private DishMapper dishMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public List<HashMap> searchDishesByShopId(HashMap map) {
        //获取shopId
        String shopId = map.get("shopId").toString();
        //获取start
        long start = Long.parseLong(map.get("start").toString());
        //获取length
        long length = Long.parseLong(map.get("length").toString());
        //计算end
        long end = start + length - 1;
        //获取dish数量
        Long size = redisTemplate.opsForList().size("dish:AllDish:" + shopId);
        //如果dish数量大于0，则从redis中获取dish
        if (size > 0){
            List range = redisTemplate.opsForList().range("dish:AllDish:" + shopId, start, end);
            return range;
        }
        //从数据库中获取dish
        ArrayList<HashMap> maps = dishMapper.searchDishesByShopId(map);
        //将dish同步到redis
        ArrayList<HashMap> list = dishMapper.syncDishesByShopId(map);
        //将dish同步到redis
        for (HashMap hashMap : list){
            redisTemplate.opsForList().rightPush("dish:AllDish:" + shopId, hashMap);
        }
        //设置redis过期时间
        redisTemplate.expire("dish:AllDish:" + shopId, 1, TimeUnit.HOURS);
        return maps;
    }

}
