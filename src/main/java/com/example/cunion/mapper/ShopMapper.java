package com.example.cunion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cunion.entity.Shop;
import org.apache.shiro.crypto.hash.Hash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
* @author 37026
* @description 针对表【shop(商家表)】的数据库操作Mapper
* @createDate 2023-09-13 22:26:21
* @Entity generator.domain.Shop
*/
public interface ShopMapper extends BaseMapper<Shop> {
    /**
     * 查询所有商家
     */
    ArrayList<HashMap> searchAllShops(HashMap hashMap);


    ArrayList<HashMap> searchAllShopsByPage(HashMap hashMap);


    ArrayList<HashMap> syncAll(HashMap hashMap);

    ArrayList<HashMap> syncAllByPage(HashMap hashMap);


    /**
     * 通过id查询商家
     */
    HashMap searchShopById(String shopId);

    /**
     * 在数据库和redis中同步商家的收藏列表
     */
    Integer syncShopCollectRedisAndDb(HashMap map);

    /**
     * 在数据库和redis中同步商家的点赞列表
     */
    Integer syncShopThumbRedisAndDb(HashMap map);

    Integer updateShopById(HashMap map);

    Integer deleteShop(String shopId);



}




