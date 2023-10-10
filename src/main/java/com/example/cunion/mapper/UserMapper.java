package com.example.cunion.mapper;

import com.example.cunion.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
* @author 37026
* @description 针对表【user】的数据库操作Mapper
* @createDate 2023-09-10 15:00:36
* @Entity com.example.cunion.entity.User
*/
@Mapper
public interface UserMapper{

    /**
     * 游客登录
     */
    Integer touristLogin(HashMap map);

    /**
     * 查询用户权限
     */
    Set<String> searchUserRoleByUserId(String userId);
    /**
     *注册用户
     */
    Integer register(HashMap hashMap);

    /**
     *根据用户账号查询用户信息
     */
    HashMap check(HashMap hashMap);

    /**
     * 用户登录
     */
    HashMap login(HashMap hashMap);

    /**
     * 根据id查询用户信息
     */
    User getLoginUser(String id);

    /**
     * 在数据库和redis中同步用户收藏列表
     */
    Integer syncUserCollectRedisAndDb(HashMap map);

    /**
     * 在数据库和redis中同步用户点赞列表
     */
    Integer syncUserThumbRedisAndDb(HashMap map);

    /**
     * 通过id查询用户昵称和头像
     */
    HashMap searchUserById(String id);

    Integer updateUserInfo(HashMap map);

    String searchMyCollectList(String userId);

    String searchMyThumbList(String userId);

    Integer updatePassword(HashMap map);

    ArrayList<HashMap> searchAllUserByPage(HashMap map);

    Integer deleteUser(String userId);

}




