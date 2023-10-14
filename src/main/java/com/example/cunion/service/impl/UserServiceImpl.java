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
        //调用userMapper的touristLogin方法，传入map参数
        Integer result = userMapper.touristLogin(map);
        //判断返回值是否为1
        if (result != 1){
            //抛出异常
            throw new CunionException("登录失败！请重试！");
        }
        //返回结果
        return result;
    }

    @Override
    public Set<String> searchUserRoleByUserId(String userId) {
        //根据用户ID查询用户角色
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
        // 根据id获取用户信息
        User user = userMapper.getLoginUser(id);
        // 获取用户头像列表
        String thumbList = user.getThumbList();
        // 获取用户收藏列表
        String collectList = user.getCollectList();
        // 判断头像列表是否为空
        if (thumbList != null && !thumbList.equals("") && !thumbList.equals("[]")){
            // 将头像列表转换为List
            List<String> list = JSONUtil.toList(thumbList, String.class);
            // 设置头像列表大小
            user.setThumbList(String.valueOf(list.size()));
        }
        // 判断收藏列表是否为空
        if (collectList != null && !collectList.equals("") && !collectList.equals("[]")){
            // 将收藏列表转换为List
            List<String> list = JSONUtil.toList(collectList, String.class);
            // 设置收藏列表大小
            user.setCollectList(String.valueOf(list.size()));
        }
        // 判断用户是否为空
        if(user == null){
            // 抛出异常
            throw new CunionException("该账号不存在！");
        }
        // 返回用户信息
        return user;
    }

    /**
     * 在数据库和redis中同步用户收藏列表
     */
    @Override
    public Integer syncUserCollectRedisAndDb() {
        // 创建一个HashMap
        HashMap map = new HashMap();
        // 获取redis中user:collect:*的key
        Set<String> keysCollect = stringRedisTemplate.keys("user:collect:*");
        // 如果key的size为0，则返回0
        if (keysCollect.size() == 0){
            return 0;
        }
        // 创建一个ArrayList
        ArrayList arrayList = new ArrayList();
        // 遍历key，将key拆分成数组，将拆分后的第三个元素添加到ArrayList中
        for (String result : keysCollect){
            String[] split = result.split(":");
            arrayList.add(split[2]);
        }
        // 遍历ArrayList，将ArrayList中的元素添加到HashMap中
        for (int i = 0; i < arrayList.size(); i++) {
            map.put("id", arrayList.get(i));
            // 从redis中取出user:collect:*的值，并将其添加到user:collect:*的列表中
            String leftPopCollect = stringRedisTemplate.opsForList().leftPop("user:collect:" + arrayList.get(i));
            stringRedisTemplate.opsForList().leftPush("user:collect:" + arrayList.get(i), leftPopCollect);
            // 将从redis中取出的值添加到HashMap中
            map.put("collect", leftPopCollect);
            // 调用userMapper的syncUserCollectRedisAndDb方法，将HashMap中的值同步到数据库中
            userMapper.syncUserCollectRedisAndDb(map);
        }
        // 返回1
        return 1;
    }

    /**
     * 在数据库和redis中同步用户点赞列表
     */
    @Override
    public Integer syncUserThumbRedisAndDb() {
        // 创建一个HashMap
        HashMap map = new HashMap();
        // 获取Redis中user:thumb:开头的key
        Set<String> keysThumb = stringRedisTemplate.keys("user:thumb:*");
        // 如果获取到的key为空，则返回0
        if (keysThumb.size() == 0){
            return 0;
        }
        // 创建一个ArrayList
        ArrayList arrayList = new ArrayList();
        // 遍历获取到的key，将key中的id添加到ArrayList中
        for (String result : keysThumb){
            String[] split = result.split(":");
            arrayList.add(split[2]);
        }
        // 遍历ArrayList，将ArrayList中的id和thumb从Redis中取出，并将thumb放入Redis中
        for (int i = 0; i < arrayList.size(); i++) {
            map.put("id", arrayList.get(i));
            // 从Redis中取出thumb
            String leftPopThumb = stringRedisTemplate.opsForList().leftPop("user:thumb:" + arrayList.get(i));
            // 将thumb放入Redis中
            stringRedisTemplate.opsForList().leftPush("user:thumb:" + arrayList.get(i), leftPopThumb);
            map.put("thumb", leftPopThumb);
            // 将Redis中的数据同步到数据库中
            userMapper.syncUserThumbRedisAndDb(map);
        }
        // 返回1
        return 1;
    }

    /**
     * 通过id查询用户昵称和头像
     */
    @Override
    public HashMap searchUserById(String id) {
        // 根据id搜索用户
        HashMap map = userMapper.searchUserById(id);
        return map;
    }

    @Override
    public Integer updateUserInfo(HashMap map) {
        //从map中获取userId
        String userId = map.get("userId").toString();
        //从redis中获取keys
        Set keys = redisTemplate.keys("comment:searchAllComments:*");
        Set keysParent = redisTemplate.keys("post:parentComment:*");
        Set keysPost= redisTemplate.keys("comment:postComment:*");
        //如果keys不为空，则删除
        if (keys != null){
            redisTemplate.delete(keys);
        }
        //如果keysParent不为空，则删除
        if (keysParent != null){
            redisTemplate.delete(keysParent);
        }
        //如果keysPost不为空，则删除
        if (keysPost != null){
            redisTemplate.delete(keysPost);
        }
        //从redis中删除post:searchAllPosts
        redisTemplate.delete("post:searchAllPosts");
        //从redis中删除post:myAllPost: + userId
        redisTemplate.delete("post:myAllPost:" + userId);
        //从redis中删除message:myMessage: + userId
        redisTemplate.delete("message:myMessage:" + userId);
        //调用userMapper的updateUserInfo方法更新用户信息
        Integer result = userMapper.updateUserInfo(map);
        try {
            //线程休眠150毫秒
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //如果keys不为空，则删除
        if (keys != null){
            redisTemplate.delete(keys);
        }
        //如果keysParent不为空，则删除
        if (keysParent != null){
            redisTemplate.delete(keysParent);
        }
        //如果keysPost不为空，则删除
        if (keysPost != null){
            redisTemplate.delete(keysPost);
        }
        //从redis中删除post:searchAllPosts
        redisTemplate.delete("post:searchAllPosts");
        //从redis中删除post:myAllPost: + userId
        redisTemplate.delete("post:myAllPost:" + userId);
        //从redis中删除message:myMessage: + userId
        redisTemplate.delete("message:myMessage:" + userId);
        //如果更新失败，则抛出异常
        if (result != 1){
            throw new CunionException("个人信息更新失败！");
        }
        //返回更新结果
        return result;
    }

    @Override
    public List searchMyCollectList(String userId) {
        //根据用户id查询收藏列表
        String collectList = userMapper.searchMyCollectList(userId);
        List list = new ArrayList();
        //如果查询结果为空，则返回空列表
        if (collectList == null || collectList.isEmpty() || "[]".equals(collectList)){
            return list;
        }
        //将查询结果转换为字符串数组
        List<String> collect = JSONUtil.toList(collectList.toString(), String.class);
        //遍历数组，根据id查询商品信息
        for (int i = collect.size() - 1; i >= 0 ; i--) {
            HashMap hashMap = shopMapper.searchShopById(collect.get(i));
            list.add(hashMap);
        }
        return list;
    }

    @Override
    public List searchMyThumbList(String userId) {
        //根据用户id查询用户的点赞列表
        String thumbList = userMapper.searchMyThumbList(userId);
        //创建一个空的列表
        List list = new ArrayList();
        //如果查询结果为空或者为null或者为空字符串，则返回空列表
        if (thumbList == null || thumbList.isEmpty() || "[]".equals(thumbList)){
            return list;
        }
        //将查询结果转换为字符串列表
        List<String> thumb = JSONUtil.toList(thumbList.toString(), String.class);
        //遍历字符串列表，根据每个字符串查询对应的商品信息，并将商品信息添加到列表中
        for (int i = thumb.size() - 1; i >= 0 ; i--) {
            //根据每个字符串查询对应的商品信息
            HashMap hashMap = shopMapper.searchShopById(thumb.get(i));
            //将商品信息添加到列表中
            list.add(hashMap);
        }
        //返回查询结果
        return list;
    }

    @Override
    public Integer updatePassword(HashMap map) {
        //获取新密码
        String newPassword = map.get("newPassword").toString();
        //获取原密码
        String password = map.get("password").toString();
        /**
         * 密码加密
         */
        //将新密码进行加密
        newPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        //将原密码进行加密
        password = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        //将加密后的新密码和原密码添加到map中
        map.put("newPassword", newPassword);
        map.put("password", password);
        //更新密码
        Integer result = userMapper.updatePassword(map);
        //如果更新失败，抛出异常
        if (result != 1){
            throw new CunionException("请检查原密码输入是否正确！");
        }
        //返回更新结果
        return result;
    }

    @Override
    public ArrayList<HashMap> searchAllUserByPage(HashMap map) {
        //根据map参数查询用户列表
        ArrayList<HashMap> list = userMapper.searchAllUserByPage(map);
        //返回查询结果
        return list;
    }

    @Override
    public Integer deleteUser(String userId) {
        //根据用户id删除用户
        Integer result = userMapper.deleteUser(userId);
        //如果删除失败，抛出异常
        if (result != 1){
            throw new CunionException("删除用户失败");
        }
        //返回删除结果
        return result;
    }
}




