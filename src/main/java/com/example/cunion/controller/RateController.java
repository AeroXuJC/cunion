package com.example.cunion.controller;

import cn.hutool.json.JSONUtil;
import com.example.cunion.common.R;
import com.example.cunion.config.shiro.JwtUtil;
import com.example.cunion.controller.form.RateForm;
import com.example.cunion.exception.CunionException;
import com.example.cunion.service.PostService;
import com.example.cunion.service.ShopService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/rate")
public class RateController {

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private ShopService shopService;

    @PostMapping("/addRate")
    public R addRate(@RequestHeader("token") String token, @RequestBody RateForm form) {
        String shopId = form.getShopId();
        Double value = form.getValue();
        String userId = jwtUtil.getUserId(token);
        Double score = 0.0;
        Long size = redisTemplate.opsForList().size("rate:" + shopId + ":" + userId);
        if (size > 0) {
            //说明已经评分过,将已经评分的分数替换成现在的，然后将所有分数相加算一遍
            Object o = redisTemplate.opsForList().rightPop("rate:" + shopId + ":" + userId);
            redisTemplate.opsForList().leftPush("rate:" + shopId + ":" + userId, value);
        } else {
            //这里是第一次评分的
            redisTemplate.opsForList().leftPush("rate:" + shopId + ":" + userId, value);
        }
        Set keys = redisTemplate.keys("rate:" + shopId + ":*");
        for (Object key : keys) {
            Object rateValue = redisTemplate.opsForList().range(key, 0, -1);
            List<Double> list = JSONUtil.toList(rateValue.toString(), Double.class);
            score += list.get(0);
        }
        score = score / keys.size();
        // 创建DecimalFormat对象，指定保留一位小数的格式
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        // 格式化数字，保留一位小数
        String formattedNumber = decimalFormat.format(score);
        //再将字符串Double转成Double
        score = Double.parseDouble(formattedNumber);
        HashMap map = new HashMap();
        map.put("shopId", shopId);
        map.put("score", score);
        redisTemplate.delete("shop:content:" + shopId);
        Set<String> allKeys = redisTemplate.keys( "shop:searchAllShops:*");
        if (allKeys != null) {
            redisTemplate.delete(allKeys);
        }
        shopService.addRate(map);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new CunionException("评分失败！");
        }
        redisTemplate.delete("shop:content:" + shopId);
        if (allKeys != null) {
            redisTemplate.delete(allKeys);
        }
        return R.ok().put("result", score);
    }
}
