package com.example.cunion.controller;

import com.example.cunion.common.R;
import com.example.cunion.controller.form.DishForm;
import com.example.cunion.service.DishService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/dish")
public class DishController {
    @Resource
    private DishService dishService;

    @PostMapping("/searchDishesByShopId")
    public R searchDishesByShopId(@RequestHeader("token") String token, @RequestBody DishForm dishForm){
        //获取传入的shopId
        String shopId = dishForm.getShopId();
        //获取传入的起始位置
        Integer start = dishForm.getStart();
        //获取传入的每页长度
        Integer length = dishForm.getLength();
        //计算起始位置
        start = (start - 1) * length;
        //创建HashMap
        HashMap<String, Object> map = new HashMap<>();
        //将shopId放入HashMap
        map.put("start", start);
        map.put("length", length);
        map.put("shopId", shopId);
        //调用dishService的searchDishesByShopId方法，传入map，获取返回值
        List<HashMap> maps = dishService.searchDishesByShopId(map);
        //返回结果
        return R.ok().put("result", maps);
    }
}
