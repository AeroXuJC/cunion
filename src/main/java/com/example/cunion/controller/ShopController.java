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
        //获取分页信息
        int start = shopForm.getStart();
        int length = shopForm.getLength();
        //获取位置信息
        String position = shopForm.getPosition();
        //获取楼层信息
        Integer floor = shopForm.getFloor();
        //获取搜索信息
        String searchValue = shopForm.getSearchValue();
        //计算起始位置
        start = (start - 1) * length;
        //创建HashMap
        HashMap hashMap = new HashMap();
        //将分页信息放入HashMap
        hashMap.put("start", start);
        hashMap.put("length", length);
        hashMap.put("position", position);
        hashMap.put("floor", floor);
        hashMap.put("searchValue", searchValue);
        //调用查询方法
        List<HashMap> maps = shopService.searchAllShops(hashMap);
        //返回查询结果
        return R.ok().put("result", maps);
    }

    /**
     * 查询所有商家
     */
    @PostMapping("/searchAllShops")
    public R searchAllShops(@RequestHeader("token") String token, @RequestBody ShopForm shopForm){
        //获取分页信息
        int start = shopForm.getStart();
        int length = shopForm.getLength();
        //获取位置信息
        String position = shopForm.getPosition();
        //获取楼层信息
        Integer floor = shopForm.getFloor();
        //获取搜索信息
        String searchValue = shopForm.getSearchValue();
        //计算起始位置
        start = (start - 1) * length;
        //创建HashMap
        HashMap hashMap = new HashMap();
        //将分页信息放入HashMap
        hashMap.put("start", start);
        hashMap.put("length", length);
        hashMap.put("position", position);
        hashMap.put("floor", floor);
        hashMap.put("searchValue", searchValue);
        //调用查询方法
        List<HashMap> maps = shopService.searchAllShopsByPage(hashMap);
        //返回查询结果
        return R.ok().put("result", maps);
    }

    /**
     * 通过id查询商家
     */
    @PostMapping("/searchShopById")
    public R searchShopById(@RequestHeader("token") String token, @RequestBody SingleShopForm singleShopForm){
        //获取商家id
        String shopId = singleShopForm.getShopId();
        //调用查询方法
        HashMap map = shopService.searchShopById(shopId);
        //返回查询结果
        return R.ok().put("result", map);
    }


    @PostMapping("/updateShopById")
    @RequiresPermissions(value = {"admin"})
    public R updateShopById(@RequestHeader("token") String token, @RequestBody UpdateShopByIdForm form){
        //将表单转换为HashMap
        HashMap<String, Object> map = (HashMap<String, Object>) BeanUtil.beanToMap(form);
        //调用更新方法
        Integer result = shopService.updateShopById(map);
        //返回更新结果
        return R.ok();
    }

    @GetMapping("/deleteShop")
    public R deleteShop(@RequestHeader("token") String token, @RequestParam("shopId") String shopId){
        //调用删除方法
        Integer result = shopService.deleteShop(shopId);
        //返回删除结果
        return R.ok();
    }
}
