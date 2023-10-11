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
        String shopId = map.get("shopId").toString();
        long start = Long.parseLong(map.get("start").toString());
        long length = Long.parseLong(map.get("length").toString());
        long end = start + length - 1;
        Long size = redisTemplate.opsForList().size("dish:AllDish:" + shopId);
        if (size > 0){
            List range = redisTemplate.opsForList().range("dish:AllDish:" + shopId, start, end);
            return range;
        }
        ArrayList<HashMap> maps = dishMapper.searchDishesByShopId(map);
        ArrayList<HashMap> list = dishMapper.syncDishesByShopId(map);
        for (HashMap hashMap : list){
            redisTemplate.opsForList().rightPush("dish:AllDish:" + shopId, hashMap);
        }
        redisTemplate.expire("dish:AllDish:" + shopId, 1, TimeUnit.HOURS);
        return maps;
    }

}
