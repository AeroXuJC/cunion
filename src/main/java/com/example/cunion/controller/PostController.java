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
        ArrayList<HashMap> list = postActClassService.searchAllPostClass();
        return R.ok().put("result", list);
    }

    @PostMapping("/searchAllPosts")
    public R searchAllPosts(@RequestHeader("token") String token, @RequestBody SearchAllPostsForm form) {
        int start = form.getStart();
        int length = form.getLength();
        String searchValue = form.getSearchValue();
        start = (start - 1) * length;
        HashMap map = new HashMap();
        map.put("start", start);
        map.put("length", length);
        map.put("searchValue", searchValue);
        List<HashMap> list = postService.searchAllPosts(map);
        return R.ok().put("result", list);
    }

    @GetMapping("/actThumbPost")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R actThumbPost(@RequestHeader("token") String token, @RequestParam("postId") String postId) {
        String userId = jwtUtil.getUserId(token);
        String value = stringRedisTemplate.opsForValue().get("post:thumb:" + postId + ":" + userId);
        HashMap map = new HashMap();
        map.put("postId", postId);
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
            redisTemplate.delete("post:myAllPost:" + userId);
            redisTemplate.delete("post:content:" + postId);
            redisTemplate.delete("post:searchAllPosts");
            postMapper.removeThumbNum(postId);
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
        redisTemplate.delete("post:myAllPost:" + userId);
        redisTemplate.delete("post:content:" + postId);
        redisTemplate.delete("post:searchAllPosts");
        postMapper.addThumbNum(postId);
        stringRedisTemplate.opsForValue().set("post:thumb:" + postId + ":" + userId, userId + postId);
        redisTemplate.delete("post:myAllPost:" + userId);
        redisTemplate.delete("post:content:" + postId);
        redisTemplate.delete("post:searchAllPosts");
        return R.ok("点赞成功").put("result", true);
    }

    @GetMapping("/isThumbPost")
    public R isThumbPost(@RequestHeader("token") String token, @RequestParam("postId") String postId) {
        String userId = jwtUtil.getUserId(token);
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
        HashMap hashMap = postService.searchAllPostById(map);
        return R.ok().put("result", hashMap);
    }

    @PostMapping("/addPost")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R addPost(@RequestHeader("token") String token, @RequestBody AddPostForm addPostForm) {
        String userId = jwtUtil.getUserId(token);
        String postContent = addPostForm.getPostContent();
        String postAddress = addPostForm.getPostAddress();
        String picture = addPostForm.getPicture();
        String tagList = addPostForm.getTagList();
        StringSnowflakeIdGenerator stringSnowflakeIdGenerator = new StringSnowflakeIdGenerator(1, 1);
        String id = stringSnowflakeIdGenerator.nextId();
        HashMap map = new HashMap();
        map.put("id", id);
        map.put("userId", userId);
        map.put("postContent", postContent);
        map.put("postAddress", postAddress);
        map.put("picture", picture);
        map.put("tagList", tagList);
        Integer result = postService.addPost(map);
        return R.ok();
    }

    @GetMapping("/deletePost")
    public R deletePost(@RequestHeader("token") String token, @RequestParam("id") String id) {
        String userId = jwtUtil.getUserId(token);
        postService.deletePost(id, userId);
        return R.ok();
    }

    @GetMapping("/searchPostByTag")
    public R searchPostByTag(@RequestHeader("token") String token, @RequestParam("classId") String classId) {
        if (classId.isEmpty()){
            HashMap<String, Object> map = new HashMap<>();
            map.put("start", 0);
            map.put("length", 100);
            map.put("searchValue", "");
            List<HashMap> list = postService.searchAllPosts(map);
            return R.ok().put("result", list);
        }
        List<HashMap> list = postService.searchPostByTag(classId);
        return R.ok().put("result", list);
    }

    @PostMapping("/searchMyPost")
    public R searchMyPost(@RequestHeader("token") String token, @RequestBody SearchMyPostForm form){
        String userId = jwtUtil.getUserId(token);
        Integer start = form.getStart();
        Integer length = form.getLength();
        start = (start - 1) * length;
        String searchValue = form.getSearchValue();
        HashMap map = new HashMap();
        map.put("userId", userId);
        map.put("start", start);
        map.put("length", length);
        map.put("searchValue", searchValue);
        List<HashMap> list = postService.searchMyPost(map);
        return R.ok().put("result", list);
    }
    @GetMapping("/searchMyPostNum")
    public R searchMyPostNum(@RequestHeader("token") String token){
        Integer result = postService.searchMyPostNum(jwtUtil.getUserId(token));
        return R.ok().put("result", result);
    }

}
