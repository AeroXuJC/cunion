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
        String shopId = collectForm.getShopId();
        String userId = jwtUtil.getUserId(token);
        collectService.addCollect(shopId, userId);
        return R.ok("收藏成功！");
    }

    @PostMapping("/removeCollect")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R removeCollect(@RequestHeader("token") String token, @RequestBody CollectForm collectForm){
        String shopId = collectForm.getShopId();
        String userId = jwtUtil.getUserId(token);
        collectService.removeCollect(shopId, userId);
        return R.ok("取消收藏成功！");
    }

    @GetMapping("/isCollect")
    public R isCollect(@RequestHeader("token") String token, @RequestParam("shopId") String shopId){
        String userId = jwtUtil.getUserId(token);
        String isExist = stringRedisTemplate.opsForValue().get("collectIsExist:" + userId + ":" + shopId);
        if(isExist != null){
            return R.ok().put("result", true);
        }
        return R.ok().put("result", false);
    }
}
