package com.example.cunion.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.cunion.common.R;
import com.example.cunion.config.shiro.JwtUtil;
import com.example.cunion.controller.form.*;
import com.example.cunion.entity.User;
import com.example.cunion.exception.CunionException;
import com.example.cunion.service.UserService;
import com.example.cunion.util.RandomStringGenerator;
import com.example.cunion.util.StringSnowflakeIdGenerator;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private JwtUtil jwtUtil;

    /**
     * 游客登录
     */
    @GetMapping("/touristLogin")
    public R touristLogin(){
        // 创建一个HashMap对象
        HashMap map = new HashMap();
        // 创建一个StringSnowflakeIdGenerator对象
        StringSnowflakeIdGenerator stringSnowflakeIdGenerator = new StringSnowflakeIdGenerator(1, 1);
        // 调用nextId()方法生成一个userId
        String userId = stringSnowflakeIdGenerator.nextId();
        // 将userId放入HashMap中
        map.put("userId", userId);
        // 生成一个随机字符串
        map.put("nickname", "tourist_" + RandomStringGenerator.generateRandomString(6));
        // 生成一个随机字符串
        map.put("userAccount", RandomStringGenerator.generateRandomString(12));
        // 生成一个随机字符串
        map.put("password", RandomStringGenerator.generateRandomString(12));
        // 生成一个随机字符串
        map.put("address", "广东金融学院");
        // 生成一个随机字符串
        map.put("gender", "男");
        // 生成一个随机字符串
        map.put("userRole", "tourist");
        // 调用userService的touristLogin()方法
        Integer result = userService.touristLogin(map);
        // 生成一个token
        String token = jwtUtil.createToken(userId);
        // 将token放入redis中
        redisTemplate.opsForValue().set("tourist:token:" + userId, token);
        // 返回一个R对象
        return R.ok().put("token", token);
    }
    /**
     *注册用户
     */
    @PostMapping("/register")
    public R register(@RequestBody @Valid RegisterForm registerForm) {
        // 获取账号
        String account = registerForm.getAccount();
        // 获取密码
        String password = registerForm.getPassword();
        // 获取确认密码
        String checkPassword = registerForm.getCheckPassword();
        // 判断账号和密码是否符合要求
        if (account.length() < 3 || password.length() < 8 || account.length() > 16 || password.length() > 16){
            throw new CunionException("账号需3~16位，密码需8~16位");
        }
        // 判断两次密码是否一致
        if (!password.equals(checkPassword)) {
            throw new CunionException("两次密码不一致！请重试！");
        }
        // 调用userService的register()方法
        Integer integer = userService.register(account, password);
        // 判断注册结果
        if (integer == 0) {
            throw new CunionException("注册失败，请重试");
        }
        // 返回一个R对象
        return R.ok("注册成功！");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public R login(@RequestBody @Valid LoginForm loginForm) {
        // 获取账号
        String account = loginForm.getAccount();
        // 获取密码
        String password = loginForm.getPassword();
        // 调用userService的login()方法
        HashMap user = userService.login(account, password);
        // 创建一个ArrayList对象
        ArrayList<HashMap> list = new ArrayList();
        // 将user放入ArrayList中
        list.add(user);
        // 获取userId
        String id = user.get("id").toString();
        // 生成一个token
        String token = jwtUtil.createToken(id);
        // 将token放入redis中
        redisTemplate.opsForValue().set("user:token:" + id, token);
        // 将token放入user中
        user.put("token", token);
        // 返回一个R对象
        return R.ok("登录成功！").put("token", token);
    }

    /**
     * 根据id查询用户信息
     */
    @GetMapping("/getLoginUser")
    public R getLoginUser(@RequestHeader("token") String token) {
        // 获取userId
        String userId = jwtUtil.getUserId(token);
        // 判断userId是否为空
        if (userId == null || userId.isEmpty()) {
            throw new CunionException("参数不正确！");
        }
        // 调用userService的getLoginUser()方法
        User result = userService.getLoginUser(userId);
        // 返回一个R对象
        return R.ok().put("result", result);

    }

    @GetMapping("/getUser")
    public R getUser(@RequestHeader("token") String token, @RequestParam("userId") String userId) {
        // 判断userId是否为空
        if (userId == null || userId.isEmpty()) {
            throw new CunionException("参数不正确！");
        }
        // 调用userService的getLoginUser()方法
        User result = userService.getLoginUser(userId);
        // 返回一个R对象
        return R.ok().put("result", result);

    }

    @PostMapping("/updateUser")
    @RequiresPermissions(value = {"admin"})
    public R updateUser(@RequestHeader("token") String token, @Valid @RequestBody UpdateUserForm form){
        // 获取邮箱
        String email = form.getEmail();
        // 获取手机号
        String phone = form.getPhone();
        // 邮箱正则表达式
        String emailPattern = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
        // 手机号正则表达式
        String phonePattern = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";
        // 判断邮箱是否符合正则表达式
        boolean matchesEmail = email.matches(emailPattern);
        // 判断手机号是否符合正则表达式
        boolean matchesPhone = phone.matches(phonePattern);
        // 判断邮箱是否符合正则表达式
        if (!matchesEmail && !email.isEmpty()){
            throw new CunionException("请输入正确的邮箱！");
        }
        // 判断手机号是否符合正则表达式
        if (!matchesPhone && !phone.isEmpty()){
            throw new CunionException("请输入正确的手机号码！");
        }
        // 将form转换为HashMap
        HashMap map = (HashMap) BeanUtil.beanToMap(form);
        // 调用userService的updateUserInfo()方法
        userService.updateUserInfo(map);
        // 返回一个R对象
        return R.ok();
    }

    /**
     * 通过id查询用户昵称和头像
     */
    @PostMapping("/searchUserById")
    public R searchUserById(@RequestHeader("token") String token, @RequestBody UserForm userForm){
        //获取用户id
        String id = userForm.getId();
        //根据id搜索用户
        HashMap map = userService.searchUserById(id);
        //返回搜索结果
        return R.ok().put("result", map);
    }

    @PostMapping("/updateUserInfo")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R updateUserInfo(@RequestHeader("token") String token, @Valid @RequestBody UpdateUserInfoForm form){
        //获取用户id
        String userId = jwtUtil.getUserId(token);
        //设置用户id
        form.setUserId(userId);
        //获取邮箱
        String email = form.getEmail();
        //获取手机号
        String phone = form.getPhone();
        //邮箱正则表达式
        String emailPattern = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
        //手机号正则表达式
        String phonePattern = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";
        //判断邮箱是否符合正则表达式
        boolean matchesEmail = email.matches(emailPattern);
        //判断手机号是否符合正则表达式
        boolean matchesPhone = phone.matches(phonePattern);
        //如果邮箱不符合正则表达式，抛出异常
        if (!matchesEmail && !email.isEmpty()){
            throw new CunionException("请输入正确的邮箱！");
        }
        //如果手机号不符合正则表达式，抛出异常
        if (!matchesPhone && !phone.isEmpty()){
            throw new CunionException("请输入正确的手机号码！");
        }
        //将表单转换为map
        HashMap map = (HashMap) BeanUtil.beanToMap(form);
        //更新用户信息
        userService.updateUserInfo(map);
        //返回更新结果
        return R.ok();
    }


    @GetMapping("/searchMyCollectList")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R searchMyCollectList(@RequestHeader("token") String token){
        //获取用户id
        String userId = jwtUtil.getUserId(token);
        //根据用户id搜索收藏列表
        List list = userService.searchMyCollectList(userId);
        //返回搜索结果
        return R.ok().put("result", list);
    }

    @GetMapping("/searchMyThumbList")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R searchMyThumbList(@RequestHeader("token") String token){
        //获取用户id
        String userId = jwtUtil.getUserId(token);
        //根据用户id搜索点赞列表
        List list = userService.searchMyThumbList(userId);
        //返回搜索结果
        return R.ok().put("result", list);
    }

    @PostMapping("/updatePassword")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R updatePassword(@RequestHeader("token") String token, @RequestBody UpdatePasswordForm form){
        //获取用户id
        String userId = jwtUtil.getUserId(token);
        //获取原密码
        String password = form.getPassword();
        //获取新密码
        String newPassword = form.getNewPassword();
        //获取确认密码
        String checkPassword = form.getCheckPassword();
        //判断原密码长度
        if (password.length() < 8 || password.length() > 16){
            throw new CunionException("密码需8~16位");
        }
        //判断两次输入的密码是否一致
        if (!newPassword.equals(checkPassword)){
            throw new CunionException("两次密码输入不一致，请重试！");
        }
        //创建map
        HashMap<String, String> map = new HashMap<>();
        //设置用户id
        map.put("id", userId);
        //设置原密码
        map.put("password", password);
        //设置新密码
        map.put("newPassword", newPassword);
        //更新密码
        Integer result = userService.updatePassword(map);
        //返回更新结果
        return R.ok();
    }

    @PostMapping("/searchAllUserByPage")
    @RequiresPermissions(value = {"admin"})
    public R searchAllUserByPage(@RequestHeader("token") String token, @RequestBody SearchAllUserByPageForm form){
        //获取分页信息
        Integer start = form.getStart();
        Integer length = form.getLength();
        //获取搜索值
        String searchValue = form.getSearchValue();
        //计算起始位置
        start = (start -1) * length;
        //创建map
        HashMap<String, Object> map = new HashMap<>();
        //将分页信息放入map
        map.put("start", start);
        map.put("length", length);
        //将搜索值放入map
        map.put("searchValue", searchValue);
        //调用service查询数据
        ArrayList<HashMap> list = userService.searchAllUserByPage(map);
        //返回查询结果
        return R.ok().put("result", list);
    }

    @GetMapping("/deleteUser")
    //删除用户
    public R deleteUser(@RequestHeader("token") String token, @RequestParam("userId") String userId){
        //调用service删除用户
        Integer result = userService.deleteUser(userId);
        //返回删除结果
        return R.ok();
    }
}
