package com.example.cunion.controller;

import com.example.cunion.common.R;
import com.example.cunion.config.shiro.JwtUtil;
import com.example.cunion.controller.form.ThumbForm;
import com.example.cunion.exception.CunionException;
import com.example.cunion.service.ThumbService;
import com.example.cunion.util.MyScheduledTask;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/thumb")
public class ThumbController {

    @Resource
    private ThumbService thumbService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Resource
    private JwtUtil jwtUtil;


    @PostMapping("/addThumb")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R addThumb(@RequestHeader("token") String token, @RequestBody ThumbForm thumbForm){
        String shopId = thumbForm.getShopId();
        String userId = jwtUtil.getUserId(token);
        thumbService.addThumb(shopId, userId);
        return R.ok("点赞成功！");
    }

    @PostMapping("/removeThumb")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R removeThumb(@RequestHeader("token") String token, @RequestBody ThumbForm thumbForm){
        String shopId = thumbForm.getShopId();
        String userId = jwtUtil.getUserId(token);
        thumbService.removeThumb(shopId, userId);
        return R.ok("取消点赞成功！");
    }

    @GetMapping("/isThumb")
    public R isThumb(@RequestHeader("token") String token, @RequestParam("shopId") String shopId){
        String userId = jwtUtil.getUserId(token);
        String shopIsExist = stringRedisTemplate.opsForValue().get("shopThumbIsExist:" + userId + ":" + shopId);
        if(shopIsExist != null){
            return R.ok().put("result", true);
        }
        return R.ok().put("result", false);
    }

}
