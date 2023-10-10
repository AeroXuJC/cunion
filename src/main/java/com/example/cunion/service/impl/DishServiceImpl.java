package com.example.cunion.service.impl;

import com.example.cunion.mapper.DishMapper;
import com.example.cunion.service.DishService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class DishServiceImpl implements DishService {

    @Resource
    private DishMapper dishMapper;
    @Override
    public ArrayList<HashMap> searchDishesByShopId(String shopId) {
        ArrayList<HashMap> maps = dishMapper.searchDishesByShopId(shopId);
        return maps;
    }
}
