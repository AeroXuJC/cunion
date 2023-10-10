package com.example.cunion.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface ShopService {
    /**
     * 查询所有商家
     */
    List<HashMap> searchAllShops(HashMap hashMap);

    List<HashMap> searchAllShopsByPage(HashMap hashMap);

    /**
     * 通过id查询商家
     */
    HashMap searchShopById(String shopId);


    /**
     * 在数据库和redis中同步商家的收藏列表
     */
    Integer syncShopCollectRedisAndDb();

    /**
     * 在数据库和redis中同步商家的点赞列表
     */
    Integer syncShopThumbRedisAndDb();


    Integer updateShopById(HashMap map);

    Integer deleteShop(String shopId);


}
