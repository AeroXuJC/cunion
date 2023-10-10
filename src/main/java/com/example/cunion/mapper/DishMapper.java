package com.example.cunion.mapper;

import com.example.cunion.entity.Dish;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author 37026
* @description 针对表【dish(菜品表)】的数据库操作Mapper
* @createDate 2023-09-19 20:52:29
* @Entity com.example.cunion.entity.Dish
*/
public interface DishMapper extends BaseMapper<Dish> {
    ArrayList<HashMap> searchDishesByShopId(String shopId);
}




