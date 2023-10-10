package com.example.cunion.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.cunion.common.R;
import com.example.cunion.controller.form.ShopForm;
import com.example.cunion.controller.form.SingleShopForm;
import com.example.cunion.controller.form.UpdateShopByIdForm;
import com.example.cunion.service.ShopService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/shop")
public class ShopController {
    @Resource
    private ShopService shopService;

    @Resource
    private RedisTemplate redisTemplate;
    /**
     * 查询所有商家
     */
    @PostMapping("/searchAllShopsByPage")
    public R searchAllShopsByPage(@RequestHeader("token") String token, @RequestBody ShopForm shopForm){
        int start = shopForm.getStart();
        int length = shopForm.getLength();
        String position = shopForm.getPosition();
        Integer floor = shopForm.getFloor();
        String searchValue = shopForm.getSearchValue();
        start = (start - 1) * length;
        HashMap hashMap = new HashMap();
        hashMap.put("start", start);
        hashMap.put("length", length);
        hashMap.put("position", position);
        hashMap.put("floor", floor);
        hashMap.put("searchValue", searchValue);
        List<HashMap> maps = shopService.searchAllShops(hashMap);
        return R.ok().put("result", maps);
    }

    /**
     * 查询所有商家
     */
    @PostMapping("/searchAllShops")
    public R searchAllShops(@RequestHeader("token") String token, @RequestBody ShopForm shopForm){
        int start = shopForm.getStart();
        int length = shopForm.getLength();
        String position = shopForm.getPosition();
        Integer floor = shopForm.getFloor();
        String searchValue = shopForm.getSearchValue();
        start = (start - 1) * length;
        HashMap hashMap = new HashMap();
        hashMap.put("start", start);
        hashMap.put("length", length);
        hashMap.put("position", position);
        hashMap.put("floor", floor);
        hashMap.put("searchValue", searchValue);
        List<HashMap> maps = shopService.searchAllShopsByPage(hashMap);
        return R.ok().put("result", maps);
    }

    /**
     * 通过id查询商家
     */
    @PostMapping("/searchShopById")
    public R searchShopById(@RequestHeader("token") String token, @RequestBody SingleShopForm singleShopForm){
        String shopId = singleShopForm.getShopId();
        HashMap map = shopService.searchShopById(shopId);
        return R.ok().put("result", map);
    }


    @PostMapping("/updateShopById")
    @RequiresPermissions(value = {"admin"})
    public R updateShopById(@RequestHeader("token") String token, @RequestBody UpdateShopByIdForm form){
        HashMap<String, Object> map = (HashMap<String, Object>) BeanUtil.beanToMap(form);
        Integer result = shopService.updateShopById(map);
        return R.ok();
    }

    @GetMapping("/deleteShop")
    public R deleteShop(@RequestHeader("token") String token, @RequestParam("shopId") String shopId){
        Integer result = shopService.deleteShop(shopId);
        return R.ok();
    }
}
