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
        // 根据shopId从redis中获取shopContent
        Object shopContent = redisTemplate.opsForValue().get("shop:content:" + shopId);
        if (shopContent != null){
            // 将shopContent转换为map
            Map<String, Object> map = BeanUtil.beanToMap(shopContent);
            // 返回map
            return (HashMap) map;
        }
        // 从数据库中查询shop
        HashMap map = shopMapper.searchShopById(shopId);
        // 将查询到的shop存入redis
        redisTemplate.opsForValue().set("shop:content:" + shopId, map);
        // 设置redis过期时间
        redisTemplate.expire("shop:content:" + shopId, 1, TimeUnit.HOURS);
        // 返回查询到的shop
        return map;
    }

    /**
     * 查询所有商家
     */
    @Override
    public List<HashMap> searchAllShops(HashMap hashMap) {
        // 获取位置、楼层、起始位置、长度
        String position = hashMap.get("position").toString();
        String floor = hashMap.get("floor").toString();
        long start = Long.parseLong(hashMap.get("start").toString());
        long length = Long.parseLong(hashMap.get("length").toString());
        long end = start + length - 1;

        Long size = 0L;
        // 根据楼层查询
        String redisKey = floor != null && !floor.isEmpty() && !floor.equals("0") ?
                "shop:searchAllShops:" + position + ":" + floor : "shop:searchAllShops:" + position;

        // 判断是否有搜索条件
        if (!hashMap.get("searchValue").toString().isEmpty()) {
            // 如果有搜索条件，从数据库中查询
            return shopMapper.searchAllShops(hashMap);
        }

        // 从redis中获取size
        size = redisTemplate.opsForList().size(redisKey);
        // 如果size大于0，从redis中获取range
        if (size > 0) {
            List range = redisTemplate.opsForList().range(redisKey, start, end);
            return range;
        }

        // 从数据库中查询
        ArrayList<HashMap> maps = shopMapper.searchAllShops(hashMap);
        // 从数据库中同步
        ArrayList<HashMap> list = shopMapper.syncAll(hashMap);
        // 将同步的数据存入redis
        for (int i = 0; i < list.size(); i++) {
            redisTemplate.opsForList().rightPush(redisKey, list.get(i));
        }
        // 设置redis过期时间
        redisTemplate.expire(redisKey, 1, TimeUnit.HOURS);
        // 返回查询到的数据
        return maps;
    }


    @Override
    public List<HashMap> searchAllShopsByPage(HashMap hashMap) {
        // 获取位置、楼层、起始位置、长度
        String position = hashMap.get("position").toString();
        String floor = hashMap.get("floor").toString();
        long start = Long.parseLong(hashMap.get("start").toString());
        long length = Long.parseLong(hashMap.get("length").toString());
        long end = start + length - 1;

        Long size = 0L;
        // 根据楼层查询
        String redisKey = floor != null && !floor.isEmpty() && !floor.equals("0") ?
                "shop:searchAllShops:" + position + ":" + floor : "shop:searchAllShops:" + position;

        // 判断是否有搜索条件
        if (!hashMap.get("searchValue").toString().isEmpty()) {
            // 如果有搜索条件，从数据库中查询
            return shopMapper.searchAllShopsByPage(hashMap);
        }
        // 从redis中获取size
        size = redisTemplate.opsForList().size(redisKey);
        // 如果size大于0，从redis中获取range
        if (size > 0) {
            List range = redisTemplate.opsForList().range(redisKey, start, end);
            return range;
        }
        // 从数据库中查询
        ArrayList<HashMap> maps = shopMapper.searchAllShopsByPage(hashMap);
        // 从数据库中同步
        ArrayList<HashMap> list = shopMapper.syncAllByPage(hashMap);
        // 将同步的数据存入redis
        for (int i = 0; i < list.size(); i++) {
            redisTemplate.opsForList().rightPush(redisKey, list.get(i));
        }
        // 设置redis过期时间
        redisTemplate.expire(redisKey, 1, TimeUnit.HOURS);
        // 返回查询到的数据
        return maps;
    }

    /**
     * 在数据库和redis中同步商家的收藏列表
     */
    @Override
    public Integer syncShopCollectRedisAndDb() {
        // 创建一个HashMap
        HashMap map = new HashMap();
        // 获取所有的key
        Set<String> keysCollect = stringRedisTemplate.keys("shop:collect:*");
        // 如果key的数量为0，则返回0
        if (keysCollect.size() == 0) {
            return 0;
        }
        // 创建一个ArrayList
        ArrayList arrayList = new ArrayList();
        // 遍历key，将key拆分成数组
        for (String result : keysCollect) {
            String[] split = result.split(":");
            // 将拆分出来的id添加到ArrayList中
            arrayList.add(split[2]);
        }
        // 遍历ArrayList
        for (int i = 0; i < arrayList.size(); i++) {
            // 将ArrayList中的id添加到HashMap中
            map.put("id", arrayList.get(i));
            // 从Redis中获取leftPopCollect
            String leftPopCollect = stringRedisTemplate.opsForList().leftPop("shop:collect:" + arrayList.get(i));
            // 将leftPopCollect添加到Redis中
            stringRedisTemplate.opsForList().leftPush("shop:collect:" + arrayList.get(i), leftPopCollect);
            // 将leftPopCollect添加到HashMap中
            map.put("collect", leftPopCollect);
            // 调用shopMapper中的syncShopCollectRedisAndDb方法
            shopMapper.syncShopCollectRedisAndDb(map);
        }
        // 返回1
        return 1;
    }

    /**
     * 在数据库和redis中同步商家的点赞列表
     */
    @Override
    public Integer syncShopThumbRedisAndDb() {
        // 创建一个HashMap
        HashMap map = new HashMap();
        // 获取所有的key，以"shop:thumb:*"为条件
        Set<String> keysThumb = stringRedisTemplate.keys("shop:thumb:*");
        // 如果获取的key的个数为0，则返回0
        if (keysThumb.size() == 0) {
            return 0;
        }
        // 创建一个ArrayList
        ArrayList arrayList = new ArrayList();
        // 遍历获取的key
        for (String result : keysThumb) {
            // 根据":",分割key
            String[] split = result.split(":");
            // 将分割后的第三个元素添加到ArrayList中
            arrayList.add(split[2]);
        }
        // 遍历ArrayList
        for (int i = 0; i < arrayList.size(); i++) {
            // 将ArrayList中的元素添加到HashMap中
            map.put("id", arrayList.get(i));
            // 从Redis中获取thumb
            String leftPopThumb = stringRedisTemplate.opsForList().leftPop("shop:thumb:" + arrayList.get(i));
            // 将获取的thumb添加到Redis中
            stringRedisTemplate.opsForList().leftPush("shop:thumb:" + arrayList.get(i), leftPopThumb);
            // 将获取的thumb添加到HashMap中
            map.put("thumb", leftPopThumb);
            // 调用syncShopThumbRedisAndDb方法，将HashMap中的元素添加到数据库中
            shopMapper.syncShopThumbRedisAndDb(map);
        }
        // 返回null
        return null;
    }

    @Override
    public Integer updateShopById(HashMap map) {
        // 获取所有shop的key
        Set<String> keys = redisTemplate.keys( "shop:searchAllShops:*");
        // 删除当前id的key
        redisTemplate.delete("shop:content:" + map.get("id").toString());
        // 如果keys不为空，则删除keys
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        // 更新shop
        Integer result = shopMapper.updateShopById(map);
        try {
            // 休眠150毫秒
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 删除当前id的key
        redisTemplate.delete("shop:content:" + map.get("id").toString());
        // 如果keys不为空，则删除keys
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        // 如果更新失败，抛出异常
        if (result != 1) {
            throw new CunionException("店铺更新失败！");
        }
        return result;
    }

    @Override
    public Integer deleteShop(String shopId) {
        // 获取所有商家的key
        Set<String> keys = redisTemplate.keys( "shop:searchAllShops:*");
        // 删除单个商家
        redisTemplate.delete("shop:content:" + shopId);
        // 判断keys是否为空
        if (keys != null) {
            // 删除多个商家
            redisTemplate.delete(keys);
        }
        // 执行删除操作
        Integer result = shopMapper.deleteShop(shopId);
        // 判断删除操作是否成功
        try {
            // 线程休眠1.5秒
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 删除单个商家
        redisTemplate.delete("shop:content:" + shopId);
        // 判断keys是否为空
        if (keys != null) {
            // 删除多个商家
            redisTemplate.delete(keys);
        }
        // 判断删除操作是否成功
        if (result != 1) {
            throw new CunionException("商家删除失败！");
        }
        return result;
    }

    @Override
    public Integer addRate(HashMap map) {
        //调用shopMapper的addRate方法，传入map参数
        Integer result = shopMapper.addRate(map);
        //如果返回值不等于1，抛出异常
        if (result != 1){
            throw new CunionException("评分失败！");
        }
        //返回结果
        return result;
    }
}
