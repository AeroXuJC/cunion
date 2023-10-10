package com.example.cunion.controller;

import com.example.cunion.common.R;
import com.example.cunion.controller.form.DishForm;
import com.example.cunion.service.DishService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/dish")
public class DishController {
    @Resource
    private DishService dishService;

    @PostMapping("/searchDishesByShopId")
    public R searchDishesByShopId(@RequestHeader("token") String token, @RequestBody DishForm dishForm){
        String shopId = dishForm.getShopId();
        ArrayList<HashMap> maps = dishService.searchDishesByShopId(shopId);
        return R.ok().put("result", maps);
    }
}
