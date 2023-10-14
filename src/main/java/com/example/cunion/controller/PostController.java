package com.example.cunion.controller;

import cn.hutool.json.JSONUtil;
import com.example.cunion.common.R;
import com.example.cunion.config.shiro.JwtUtil;
import com.example.cunion.controller.form.AddPostForm;
import com.example.cunion.controller.form.SearchAllPostsForm;
import com.example.cunion.controller.form.SearchMyPostForm;
import com.example.cunion.exception.CunionException;
import com.example.cunion.mapper.PostMapper;
import com.example.cunion.service.PostActClassService;
import com.example.cunion.service.PostService;
import com.example.cunion.util.StringSnowflakeIdGenerator;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {
    @Resource
    private PostActClassService postActClassService;

    @Resource
    private PostService postService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private PostMapper postMapper;

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private RedisTemplate redisTemplate;

    @GetMapping("/searchAllPostClass")
    public R searchAllPostClass(@RequestHeader("token") String token) {
        //获取所有分类
        ArrayList<HashMap> list = postActClassService.searchAllPostClass();
        return R.ok().put("result", list);
    }

    @PostMapping("/searchAllPosts")
    public R searchAllPosts(@RequestHeader("token") String token, @RequestBody SearchAllPostsForm form) {
        //获取搜索参数
        int start = form.getStart();
        int length = form.getLength();
        String searchValue = form.getSearchValue();
        start = (start - 1) * length;
        HashMap map = new HashMap();
        map.put("start", start);
        map.put("length", length);
        map.put("searchValue", searchValue);
        //根据搜索参数搜索
        List<HashMap> list = postService.searchAllPosts(map);
        return R.ok().put("result", list);
    }

    @GetMapping("/actThumbPost")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R actThumbPost(@RequestHeader("token") String token, @RequestParam("postId") String postId) {
        //获取当前用户id
        String userId = jwtUtil.getUserId(token);
        //获取当前用户是否已经点赞
        String value = stringRedisTemplate.opsForValue().get("post:thumb:" + postId + ":" + userId);
        HashMap map = new HashMap();
        map.put("postId", postId);
        //获取点赞列表
        String thumbList = postMapper.searchThumbListById(postId);
        if (value != null && !"".equals(value)) {
            if (thumbList != null && !"[]".equals(thumbList) && !"".equals(thumbList)) {
                List<String> list = JSONUtil.toList(thumbList, String.class);
                list.remove(userId);
                map.put("postId", postId);
                map.put("thumbList", list.toString());
                Integer result = postMapper.updateThumbList(map);
                if (result != 1) {
                    throw new CunionException("取消点赞失败！");
                }
            }
            //更新点赞列表
            redisTemplate.delete("post:myAllPost:" + userId);
            redisTemplate.delete("post:content:" + postId);
            redisTemplate.delete("post:searchAllPosts");
            //更新点赞数
            postMapper.removeThumbNum(postId);
            //更新点赞
            stringRedisTemplate.delete("post:thumb:" + postId + ":" + userId);
            redisTemplate.delete("post:myAllPost:" + userId);
            redisTemplate.delete("post:content:" + postId);
            redisTemplate.delete("post:searchAllPosts");
            return R.ok("取消点赞").put("result", false);
        }
        //下面是添加点赞
        if (thumbList != null && !"[]".equals(thumbList) && !"".equals(thumbList)) {
            List<String> list = JSONUtil.toList(thumbList, String.class);
            list.add(userId);
            map.put("postId", postId);
            map.put("thumbList", list.toString());
            Integer result = postMapper.updateThumbList(map);
            if (result != 1) {
                throw new CunionException("点赞失败！");
            }
        } else {
            ArrayList arrayList = new ArrayList();
            arrayList.add(userId);
            map.put("id", postId);
            map.put("thumbList", arrayList.toString());
            Integer result = postMapper.updateThumbList(map);
            if (result != 1) {
                throw new CunionException("点赞失败！");
            }
        }
        //更新点赞列表
        redisTemplate.delete("post:myAllPost:" + userId);
        redisTemplate.delete("post:content:" + postId);
        redisTemplate.delete("post:searchAllPosts");
        //更新点赞数
        postMapper.addThumbNum(postId);
        //更新点赞
        stringRedisTemplate.opsForValue().set("post:thumb:" + postId + ":" + userId, userId + postId);
        redisTemplate.delete("post:myAllPost:" + userId);
        redisTemplate.delete("post:content:" + postId);
        redisTemplate.delete("post:searchAllPosts");
        return R.ok("点赞成功").put("result", true);
    }

    @GetMapping("/isThumbPost")
    public R isThumbPost(@RequestHeader("token") String token, @RequestParam("postId") String postId) {
        //获取当前用户id
        String userId = jwtUtil.getUserId(token);
        //获取当前用户是否已经点赞
        String value = stringRedisTemplate.opsForValue().get("post:thumb:" + postId + ":" + userId);
        if (value != null && !"".equals(value)) {
            return R.ok().put("result", true);
        }
        return R.ok().put("result", false);
    }

    @GetMapping("/searchAllPostById")
    public R searchAllPostById(@RequestHeader("token") String token, @RequestParam("postId") String postId) {
        HashMap map = new HashMap();
        map.put("id", postId);
        //根据id搜索
        HashMap hashMap = postService.searchAllPostById(map);
        return R.ok().put("result", hashMap);
    }

    @PostMapping("/addPost")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R addPost(@RequestHeader("token") String token, @RequestBody AddPostForm addPostForm) {
        //获取用户id
        String userId = jwtUtil.getUserId(token);
        //获取帖子内容
        String postContent = addPostForm.getPostContent();
        //获取帖子地址
        String postAddress = addPostForm.getPostAddress();
        //获取帖子图片
        String picture = addPostForm.getPicture();
        //获取帖子标签
        String tagList = addPostForm.getTagList();
        //创建一个StringSnowflakeIdGenerator对象
        StringSnowflakeIdGenerator stringSnowflakeIdGenerator = new StringSnowflakeIdGenerator(1, 1);
        //生成一个id
        String id = stringSnowflakeIdGenerator.nextId();
        //创建一个HashMap对象
        HashMap map = new HashMap();
        //将id、userId、postContent、postAddress、picture、tagList放入HashMap中
        map.put("id", id);
        map.put("userId", userId);
        map.put("postContent", postContent);
        map.put("postAddress", postAddress);
        map.put("picture", picture);
        map.put("tagList", tagList);
        //调用postService的addPost方法，添加帖子
        Integer result = postService.addPost(map);
        //返回添加帖子的结果
        return R.ok();
    }

    @GetMapping("/deletePost")
    public R deletePost(@RequestHeader("token") String token, @RequestParam("id") String id) {
        //获取用户id
        String userId = jwtUtil.getUserId(token);
        //调用postService的deletePost方法，删除帖子
        postService.deletePost(id, userId);
        //返回删除帖子的结果
        return R.ok();
    }

    @GetMapping("/searchPostByTag")
    public R searchPostByTag(@RequestHeader("token") String token, @RequestParam("classId") String classId) {
        //如果classId为空
        if (classId.isEmpty()){
            //创建一个HashMap对象
            HashMap<String, Object> map = new HashMap<>();
            //设置查询起始位置
            map.put("start", 0);
            //设置查询长度
            map.put("length", 100);
            //设置查询内容
            map.put("searchValue", "");
            //调用postService的searchAllPosts方法，查询所有帖子
            List<HashMap> list = postService.searchAllPosts(map);
            //返回查询结果
            return R.ok().put("result", list);
        }
        //调用postService的searchPostByTag方法，根据标签查询帖子
        List<HashMap> list = postService.searchPostByTag(classId);
        //返回查询结果
        return R.ok().put("result", list);
    }

    @PostMapping("/searchMyPost")
    public R searchMyPost(@RequestHeader("token") String token, @RequestBody SearchMyPostForm form){
        //获取用户id
        String userId = jwtUtil.getUserId(token);
        //获取查询起始位置
        Integer start = form.getStart();
        //获取查询长度
        Integer length = form.getLength();
        //计算查询起始位置
        start = (start - 1) * length;
        //获取查询内容
        String searchValue = form.getSearchValue();
        //创建一个HashMap对象
        HashMap map = new HashMap();
        //将userId、start、length、searchValue放入HashMap中
        map.put("userId", userId);
        map.put("start", start);
        map.put("length", length);
        map.put("searchValue", searchValue);
        //调用postService的searchMyPost方法，查询自己的帖子
        List<HashMap> list = postService.searchMyPost(map);
        //返回查询结果
        return R.ok().put("result", list);
    }
    @GetMapping("/searchMyPostNum")
    public R searchMyPostNum(@RequestHeader("token") String token){
        //调用postService的searchMyPostNum方法，查询自己的帖子数量
        Integer result = postService.searchMyPostNum(jwtUtil.getUserId(token));
        //返回查询结果
        return R.ok().put("result", result);
    }

}
