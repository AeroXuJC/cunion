package com.example.cunion.service.impl;

import cn.hutool.json.JSONUtil;
import com.example.cunion.config.shiro.JwtUtil;
import com.example.cunion.entity.User;
import com.example.cunion.exception.CunionException;
import com.example.cunion.mapper.ShopMapper;
import com.example.cunion.mapper.UserMapper;
import com.example.cunion.service.UserService;
import com.example.cunion.util.RandomStringGenerator;
import com.example.cunion.util.StringSnowflakeIdGenerator;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * @author 37026
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2023-09-10 15:00:36
 */
@Service
@Configuration
public class UserServiceImpl implements UserService {

    @Resource
    public UserMapper userMapper;

    @Resource
    private ShopMapper shopMapper;

    /**
     * 全局变量SALT
     */
    private String SALT = "miJia";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public Integer touristLogin(HashMap map) {
        Integer result = userMapper.touristLogin(map);
        if (result != 1){
            throw new CunionException("登录失败！请重试！");
        }
        return result;
    }

    @Override
    public Set<String> searchUserRoleByUserId(String userId) {
        Set<String> permissions = userMapper.searchUserRoleByUserId(userId);
        return permissions;
    }

    /**
     *注册用户
     */
    @Override
    public Integer register(String account, String password) {
        StringSnowflakeIdGenerator stringSnowflakeIdGenerator = new StringSnowflakeIdGenerator(1, 1);
        String id = stringSnowflakeIdGenerator.nextId();
        HashMap hashMap = new HashMap();
        hashMap.put("userAccount", account);
        HashMap map = userMapper.check(hashMap);
        if (map != null) {
            throw new CunionException("该账号已存在！");
        }
        /**
         * 密码加密
         */
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        hashMap.put("password", encryptPassword);
        hashMap.put("id", id);
        hashMap.put("nickname", "user_" + RandomStringGenerator.generateRandomString(8));
        Integer result = userMapper.register(hashMap);
        return result;
    }

    /**
     * 用户登录
     */
    @Override
    public HashMap login(String account, String password) {
        HashMap hashMap = new HashMap();
        hashMap.put("userAccount", account);
        /**
         * 密码加密
         */
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        hashMap.put("password", encryptPassword);
        HashMap map = userMapper.login(hashMap);
        if(map == null){
            throw new CunionException("该账号不存在！");
        }
        return map;
    }

    /**
     * 根据id查询用户信息
     */
    @Override
    public User getLoginUser(String id) {
        User user = userMapper.getLoginUser(id);
        String thumbList = user.getThumbList();
        String collectList = user.getCollectList();
        if (thumbList != null && !thumbList.equals("") && !thumbList.equals("[]")){
            List<String> list = JSONUtil.toList(thumbList, String.class);
            user.setThumbList(String.valueOf(list.size()));
        }
        if (collectList != null && !collectList.equals("") && !collectList.equals("[]")){
            List<String> list = JSONUtil.toList(collectList, String.class);
            user.setCollectList(String.valueOf(list.size()));
        }
        if(user == null){
            throw new CunionException("该账号不存在！");
        }
        return user;
    }

    /**
     * 在数据库和redis中同步用户收藏列表
     */
    @Override
    public Integer syncUserCollectRedisAndDb() {
        HashMap map = new HashMap();
        Set<String> keysCollect = stringRedisTemplate.keys("user:collect:*");
        if (keysCollect.size() == 0){
            return 0;
        }
        ArrayList arrayList = new ArrayList();
        for (String result : keysCollect){
            String[] split = result.split(":");
            arrayList.add(split[2]);
        }
        for (int i = 0; i < arrayList.size(); i++) {
            map.put("id", arrayList.get(i));
            String leftPopCollect = stringRedisTemplate.opsForList().leftPop("user:collect:" + arrayList.get(i));
            stringRedisTemplate.opsForList().leftPush("user:collect:" + arrayList.get(i), leftPopCollect);
            map.put("collect", leftPopCollect);
            userMapper.syncUserCollectRedisAndDb(map);
        }
        return 1;
    }

    /**
     * 在数据库和redis中同步用户点赞列表
     */
    @Override
    public Integer syncUserThumbRedisAndDb() {
        HashMap map = new HashMap();
        Set<String> keysThumb = stringRedisTemplate.keys("user:thumb:*");
        if (keysThumb.size() == 0){
            return 0;
        }
        ArrayList arrayList = new ArrayList();
        for (String result : keysThumb){
            String[] split = result.split(":");
            arrayList.add(split[2]);
        }
        for (int i = 0; i < arrayList.size(); i++) {
            map.put("id", arrayList.get(i));
            String leftPopThumb = stringRedisTemplate.opsForList().leftPop("user:thumb:" + arrayList.get(i));
            stringRedisTemplate.opsForList().leftPush("user:thumb:" + arrayList.get(i), leftPopThumb);
            map.put("thumb", leftPopThumb);
            userMapper.syncUserThumbRedisAndDb(map);
        }
        return 1;
    }

    /**
     * 通过id查询用户昵称和头像
     */
    @Override
    public HashMap searchUserById(String id) {
        HashMap map = userMapper.searchUserById(id);
        return map;
    }

    @Override
    public Integer updateUserInfo(HashMap map) {
        String userId = map.get("userId").toString();
        Set keys = redisTemplate.keys("comment:searchAllComments:*");
        Set keysParent = redisTemplate.keys("post:parentComment:*");
        Set keysPost= redisTemplate.keys("comment:postComment:*");
        if (keys != null){
            redisTemplate.delete(keys);
        }
        if (keysParent != null){
            redisTemplate.delete(keysParent);
        }
        if (keysPost != null){
            redisTemplate.delete(keysPost);
        }
        redisTemplate.delete("post:searchAllPosts");
        redisTemplate.delete("post:myAllPost:" + userId);
        redisTemplate.delete("message:myMessage:" + userId);
        Integer result = userMapper.updateUserInfo(map);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (keys != null){
            redisTemplate.delete(keys);
        }
        if (keysParent != null){
            redisTemplate.delete(keysParent);
        }
        if (keysPost != null){
            redisTemplate.delete(keysPost);
        }
        redisTemplate.delete("post:searchAllPosts");
        redisTemplate.delete("post:myAllPost:" + userId);
        redisTemplate.delete("message:myMessage:" + userId);
        if (result != 1){
            throw new CunionException("个人信息更新失败！");
        }
        return result;
    }

    @Override
    public List searchMyCollectList(String userId) {
        String collectList = userMapper.searchMyCollectList(userId);
        List list = new ArrayList();
        if (collectList == null || collectList.isEmpty() || "[]".equals(collectList)){
            return list;
        }
        List<String> collect = JSONUtil.toList(collectList.toString(), String.class);
        for (int i = collect.size() - 1; i >= 0 ; i--) {
            HashMap hashMap = shopMapper.searchShopById(collect.get(i));
            list.add(hashMap);
        }
        return list;
    }

    @Override
    public List searchMyThumbList(String userId) {
        String thumbList = userMapper.searchMyThumbList(userId);
        List list = new ArrayList();
        if (thumbList == null || thumbList.isEmpty() || "[]".equals(thumbList)){
            return list;
        }
        List<String> thumb = JSONUtil.toList(thumbList.toString(), String.class);
        for (int i = thumb.size() - 1; i >= 0 ; i--) {
            HashMap hashMap = shopMapper.searchShopById(thumb.get(i));
            list.add(hashMap);
        }
        return list;
    }

    @Override
    public Integer updatePassword(HashMap map) {
        String newPassword = map.get("newPassword").toString();
        String password = map.get("password").toString();
        /**
         * 密码加密
         */
        newPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        password = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        map.put("newPassword", newPassword);
        map.put("password", password);
        Integer result = userMapper.updatePassword(map);
        if (result != 1){
            throw new CunionException("请检查原密码输入是否正确！");
        }
        return result;
    }

    @Override
    public ArrayList<HashMap> searchAllUserByPage(HashMap map) {
        ArrayList<HashMap> list = userMapper.searchAllUserByPage(map);
        return list;
    }

    @Override
    public Integer deleteUser(String userId) {
        Integer result = userMapper.deleteUser(userId);
        if (result != 1){
            throw new CunionException("删除用户失败");
        }
        return result;
    }
}




