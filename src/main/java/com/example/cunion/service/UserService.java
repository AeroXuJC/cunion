package com.example.cunion.service;

import com.example.cunion.entity.User;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
* @author 37026
* @description 针对表【user】的数据库操作Service
* @createDate 2023-09-10 15:00:36
*/

public interface UserService{

    Integer touristLogin(HashMap map);

    Set<String> searchUserRoleByUserId(String userId);

    /**
     *注册用户
     */
    Integer register(String account, String password);

    /**
     * 用户登录
     */
    HashMap login(String account, String password);

    /**
     * 根据id查询用户信息
     */
    User getLoginUser(String id);

    /**
     * 在数据库和redis中同步用户收藏列表
     */
    Integer syncUserCollectRedisAndDb();

    /**
     * 在数据库和redis中同步用户点赞列表
     */
    Integer syncUserThumbRedisAndDb();

    /**
     * 通过id查询用户昵称和头像
     */
    HashMap searchUserById(String id);


    Integer updateUserInfo(HashMap map);

    List  searchMyCollectList(String userId);

    List searchMyThumbList(String userId);

    Integer updatePassword(HashMap map);

    ArrayList<HashMap> searchAllUserByPage(HashMap map);

    Integer deleteUser(String userId);



}
