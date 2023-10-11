package com.example.cunion.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.example.cunion.exception.CunionException;
import com.example.cunion.mapper.ShopMapper;
import com.example.cunion.service.ShopService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ShopServiceImpl implements ShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ShopMapper shopMapper;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 通过id查询商家
     */
    @Override
    public HashMap searchShopById(String shopId) {
        Object shopContent = redisTemplate.opsForValue().get("shop:content:" + shopId);
        if (shopContent != null){
            Map<String, Object> map = BeanUtil.beanToMap(shopContent);
            return (HashMap) map;
        }
        HashMap map = shopMapper.searchShopById(shopId);
        redisTemplate.opsForValue().set("shop:content:" + shopId, map);
        redisTemplate.expire("shop:content:" + shopId, 1, TimeUnit.HOURS);
        return map;
    }

    /**
     * 查询所有商家
     */
    @Override
    public List<HashMap> searchAllShops(HashMap hashMap) {
        String position = hashMap.get("position").toString();
        String floor = hashMap.get("floor").toString();
        long start = Long.parseLong(hashMap.get("start").toString());
        long length = Long.parseLong(hashMap.get("length").toString());
        long end = start + length - 1;

        Long size = 0L;
        String redisKey = floor != null && !floor.isEmpty() && !floor.equals("0") ?
                "shop:searchAllShops:" + position + ":" + floor : "shop:searchAllShops:" + position;

        if (!hashMap.get("searchValue").toString().isEmpty()) {
            return shopMapper.searchAllShops(hashMap);
        }

        size = redisTemplate.opsForList().size(redisKey);
        if (size > 0) {
            List range = redisTemplate.opsForList().range(redisKey, start, end);
            return range;
        }

        ArrayList<HashMap> maps = shopMapper.searchAllShops(hashMap);
        ArrayList<HashMap> list = shopMapper.syncAll(hashMap);
        for (int i = 0; i < list.size(); i++) {
            redisTemplate.opsForList().rightPush(redisKey, list.get(i));
        }
        redisTemplate.expire(redisKey, 1, TimeUnit.HOURS);
        return maps;
    }


    @Override
    public List<HashMap> searchAllShopsByPage(HashMap hashMap) {
        String position = hashMap.get("position").toString();
        String floor = hashMap.get("floor").toString();
        long start = Long.parseLong(hashMap.get("start").toString());
        long length = Long.parseLong(hashMap.get("length").toString());
        long end = start + length - 1;

        Long size = 0L;
        String redisKey = floor != null && !floor.isEmpty() && !floor.equals("0") ?
                "shop:searchAllShops:" + position + ":" + floor : "shop:searchAllShops:" + position;

        if (!hashMap.get("searchValue").toString().isEmpty()) {
            return shopMapper.searchAllShopsByPage(hashMap);
        }
        size = redisTemplate.opsForList().size(redisKey);
        if (size > 0) {
            List range = redisTemplate.opsForList().range(redisKey, start, end);
            return range;
        }
        ArrayList<HashMap> maps = shopMapper.searchAllShopsByPage(hashMap);
        ArrayList<HashMap> list = shopMapper.syncAllByPage(hashMap);
        for (int i = 0; i < list.size(); i++) {
            redisTemplate.opsForList().rightPush(redisKey, list.get(i));
        }
        redisTemplate.expire(redisKey, 1, TimeUnit.HOURS);
        return maps;
    }

    /**
     * 在数据库和redis中同步商家的收藏列表
     */
    @Override
    public Integer syncShopCollectRedisAndDb() {
        HashMap map = new HashMap();
        Set<String> keysCollect = stringRedisTemplate.keys("shop:collect:*");
        if (keysCollect.size() == 0) {
            return 0;
        }
        ArrayList arrayList = new ArrayList();
        for (String result : keysCollect) {
            String[] split = result.split(":");
            arrayList.add(split[2]);
        }
        for (int i = 0; i < arrayList.size(); i++) {
            map.put("id", arrayList.get(i));
            String leftPopCollect = stringRedisTemplate.opsForList().leftPop("shop:collect:" + arrayList.get(i));
            stringRedisTemplate.opsForList().leftPush("shop:collect:" + arrayList.get(i), leftPopCollect);
            map.put("collect", leftPopCollect);
            shopMapper.syncShopCollectRedisAndDb(map);
        }
        return 1;
    }

    /**
     * 在数据库和redis中同步商家的点赞列表
     */
    @Override
    public Integer syncShopThumbRedisAndDb() {
        HashMap map = new HashMap();
        Set<String> keysThumb = stringRedisTemplate.keys("shop:thumb:*");
        if (keysThumb.size() == 0) {
            return 0;
        }
        ArrayList arrayList = new ArrayList();
        for (String result : keysThumb) {
            String[] split = result.split(":");
            arrayList.add(split[2]);
        }
        for (int i = 0; i < arrayList.size(); i++) {
            map.put("id", arrayList.get(i));
            String leftPopThumb = stringRedisTemplate.opsForList().leftPop("shop:thumb:" + arrayList.get(i));
            stringRedisTemplate.opsForList().leftPush("shop:thumb:" + arrayList.get(i), leftPopThumb);
            map.put("thumb", leftPopThumb);
            shopMapper.syncShopThumbRedisAndDb(map);
        }
        return null;
    }

    @Override
    public Integer updateShopById(HashMap map) {
        Set<String> keys = redisTemplate.keys( "shop:searchAllShops:*");
        redisTemplate.delete("shop:content:" + map.get("id").toString());
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        Integer result = shopMapper.updateShopById(map);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        redisTemplate.delete("shop:content:" + map.get("id").toString());
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        if (result != 1) {
            throw new CunionException("店铺更新失败！");
        }
        return result;
    }

    @Override
    public Integer deleteShop(String shopId) {
        Set<String> keys = redisTemplate.keys( "shop:searchAllShops:*");
        redisTemplate.delete("shop:content:" + shopId);
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        Integer result = shopMapper.deleteShop(shopId);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        redisTemplate.delete("shop:content:" + shopId);
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        if (result != 1) {
            throw new CunionException("商家删除失败！");
        }
        return result;
    }

    @Override
    public Integer addRate(HashMap map) {
        Integer result = shopMapper.addRate(map);
        if (result != 1){
            throw new CunionException("评分失败！");
        }
        return result;
    }
}
