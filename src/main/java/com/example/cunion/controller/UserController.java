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
        HashMap map = new HashMap();
        StringSnowflakeIdGenerator stringSnowflakeIdGenerator = new StringSnowflakeIdGenerator(1, 1);
        String userId = stringSnowflakeIdGenerator.nextId();
        map.put("userId", userId);
        map.put("nickname", "tourist_" + RandomStringGenerator.generateRandomString(12));
        map.put("userAccount", RandomStringGenerator.generateRandomString(12));
        map.put("password", RandomStringGenerator.generateRandomString(12));
        map.put("address", "广东金融学院");
        map.put("gender", "男");
        map.put("userRole", "tourist");
        Integer result = userService.touristLogin(map);
        String token = jwtUtil.createToken(userId);
        redisTemplate.opsForValue().set("tourist:token:" + userId, token);
        return R.ok().put("token", token);
    }
    /**
     *注册用户
     */
    @PostMapping("/register")
    public R register(@RequestBody @Valid RegisterForm registerForm) {
        String account = registerForm.getAccount();
        String password = registerForm.getPassword();
        String checkPassword = registerForm.getCheckPassword();
        if (account.length() < 3 || password.length() < 8 || account.length() > 16 || password.length() > 16){
            throw new CunionException("账号需3~16位，密码需8~16位");
        }
        if (!password.equals(checkPassword)) {
            throw new CunionException("两次密码不一致！请重试！");
        }
        Integer integer = userService.register(account, password);
        if (integer == 0) {
            throw new CunionException("注册失败，请重试");
        }
        return R.ok("注册成功！");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public R login(@RequestBody @Valid LoginForm loginForm) {
        String account = loginForm.getAccount();
        String password = loginForm.getPassword();
        HashMap user = userService.login(account, password);
        ArrayList<HashMap> list = new ArrayList();
        list.add(user);
        String id = user.get("id").toString();
        String token = jwtUtil.createToken(id);
        redisTemplate.opsForValue().set("user:token:" + id, token);
        user.put("token", token);
        return R.ok("登录成功！").put("token", token);
    }

    /**
     * 根据id查询用户信息
     */
    @GetMapping("/getLoginUser")
    public R getLoginUser(@RequestHeader("token") String token) {
        String userId = jwtUtil.getUserId(token);
        if (userId == null || userId.isEmpty()) {
            throw new CunionException("参数不正确！");
        }
        User result = userService.getLoginUser(userId);
        return R.ok().put("result", result);

    }

    @GetMapping("/getUser")
    public R getUser(@RequestHeader("token") String token, @RequestParam("userId") String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new CunionException("参数不正确！");
        }
        User result = userService.getLoginUser(userId);
        return R.ok().put("result", result);

    }

    @PostMapping("/updateUser")
    @RequiresPermissions(value = {"admin"})
    public R updateUser(@RequestHeader("token") String token, @Valid @RequestBody UpdateUserForm form){
        String email = form.getEmail();
        String phone = form.getPhone();
        String emailPattern = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
        String phonePattern = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";
        boolean matchesEmail = email.matches(emailPattern);
        boolean matchesPhone = phone.matches(phonePattern);
        if (!matchesEmail && !email.isEmpty()){
            throw new CunionException("请输入正确的邮箱！");
        }
        if (!matchesPhone && !phone.isEmpty()){
            throw new CunionException("请输入正确的手机号码！");
        }
        HashMap map = (HashMap) BeanUtil.beanToMap(form);
        userService.updateUserInfo(map);
        return R.ok();
    }

    /**
     * 通过id查询用户昵称和头像
     */
    @PostMapping("/searchUserById")
    public R searchUserById(@RequestHeader("token") String token, @RequestBody UserForm userForm){
        String id = userForm.getId();
        HashMap map = userService.searchUserById(id);
        return R.ok().put("result", map);
    }

    @PostMapping("/updateUserInfo")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R updateUserInfo(@RequestHeader("token") String token, @Valid @RequestBody UpdateUserInfoForm form){
        String userId = jwtUtil.getUserId(token);
        form.setId(userId);
        String email = form.getEmail();
        String phone = form.getPhone();
        String emailPattern = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
        String phonePattern = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";
        boolean matchesEmail = email.matches(emailPattern);
        boolean matchesPhone = phone.matches(phonePattern);
        if (!matchesEmail && !email.isEmpty()){
            throw new CunionException("请输入正确的邮箱！");
        }
        if (!matchesPhone && !phone.isEmpty()){
            throw new CunionException("请输入正确的手机号码！");
        }
        HashMap map = (HashMap) BeanUtil.beanToMap(form);
        userService.updateUserInfo(map);
        return R.ok();
    }


    @GetMapping("/searchMyCollectList")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R searchMyCollectList(@RequestHeader("token") String token){
        String userId = jwtUtil.getUserId(token);
        List list = userService.searchMyCollectList(userId);
        return R.ok().put("result", list);
    }

    @GetMapping("/searchMyThumbList")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R searchMyThumbList(@RequestHeader("token") String token){
        String userId = jwtUtil.getUserId(token);
        List list = userService.searchMyThumbList(userId);
        return R.ok().put("result", list);
    }

    @PostMapping("/updatePassword")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R updatePassword(@RequestHeader("token") String token, @RequestBody UpdatePasswordForm form){
        String userId = jwtUtil.getUserId(token);
        String password = form.getPassword();
        String newPassword = form.getNewPassword();
        String checkPassword = form.getCheckPassword();
        if (password.length() < 8 || password.length() > 16){
            throw new CunionException("密码需8~16位");
        }
        if (!newPassword.equals(checkPassword)){
            throw new CunionException("两次密码输入不一致，请重试！");
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("id", userId);
        map.put("password", password);
        map.put("newPassword", newPassword);
        Integer result = userService.updatePassword(map);
        return R.ok();
    }

    @PostMapping("/searchAllUserByPage")
    @RequiresPermissions(value = {"admin"})
    public R searchAllUserByPage(@RequestHeader("token") String token, @RequestBody SearchAllUserByPageForm form){
        Integer start = form.getStart();
        Integer length = form.getLength();
        String searchValue = form.getSearchValue();
        start = (start -1) * length;
        HashMap<String, Object> map = new HashMap<>();
        map.put("start", start);
        map.put("length", length);
        map.put("searchValue", searchValue);
        ArrayList<HashMap> list = userService.searchAllUserByPage(map);
        return R.ok().put("result", list);
    }

    @GetMapping("/deleteUser")
    public R deleteUser(@RequestHeader("token") String token, @RequestParam("userId") String userId){
        Integer result = userService.deleteUser(userId);
        return R.ok();
    }
}
