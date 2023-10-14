package com.example.cunion.controller;

import com.example.cunion.common.R;
import com.example.cunion.config.shiro.JwtUtil;
import com.example.cunion.controller.form.CollectForm;
import com.example.cunion.controller.form.ThumbForm;
import com.example.cunion.service.CollectService;
import com.example.cunion.util.MyScheduledTask;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/collect")
public class CollectController {

    @Resource
    private CollectService collectService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtUtil jwtUtil;

    @PostMapping("/addCollect")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R addCollect(@RequestHeader("token") String token, @RequestBody CollectForm collectForm){
        //获取shopId
        String shopId = collectForm.getShopId();
        //获取userId
        String userId = jwtUtil.getUserId(token);
        //调用收藏服务添加收藏
        collectService.addCollect(shopId, userId);
        //返回收藏成功信息
        return R.ok("收藏成功！");
    }

    @PostMapping("/removeCollect")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R removeCollect(@RequestHeader("token") String token, @RequestBody CollectForm collectForm){
        //获取shopId
        String shopId = collectForm.getShopId();
        //获取userId
        String userId = jwtUtil.getUserId(token);
        //调用收藏服务取消收藏
        collectService.removeCollect(shopId, userId);
        //返回取消收藏成功信息
        return R.ok("取消收藏成功！");
    }

    @GetMapping("/isCollect")
    public R isCollect(@RequestHeader("token") String token, @RequestParam("shopId") String shopId){
        //获取userId
        String userId = jwtUtil.getUserId(token);
        //从Redis中获取是否收藏
        String isExist = stringRedisTemplate.opsForValue().get("collectIsExist:" + userId + ":" + shopId);
        //如果存在，返回true
        if(isExist != null){
            return R.ok().put("result", true);
        }
        //如果不存在，返回false
        return R.ok().put("result", false);
    }
}
