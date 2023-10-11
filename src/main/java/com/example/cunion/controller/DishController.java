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
        String shopId = dishForm.getShopId();
        Integer start = dishForm.getStart();
        Integer length = dishForm.getLength();
        start = (start - 1) * length;
        HashMap<String, Object> map = new HashMap<>();
        map.put("start", start);
        map.put("length", length);
        map.put("shopId", shopId);
        List<HashMap> maps = dishService.searchDishesByShopId(map);
        return R.ok().put("result", maps);
    }
}
