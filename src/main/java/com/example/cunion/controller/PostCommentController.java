package com.example.cunion.controller;

import cn.hutool.json.JSONUtil;
import com.example.cunion.common.R;
import com.example.cunion.config.shiro.JwtUtil;
import com.example.cunion.controller.form.*;
import com.example.cunion.exception.CunionException;
import com.example.cunion.service.CommentService;
import com.example.cunion.service.PostCommentService;
import com.example.cunion.util.StringSnowflakeIdGenerator;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/postComment")
public class PostCommentController {
    @Resource
    private PostCommentService postCommentService;

    @Resource
    private JwtUtil jwtUtil;

    @PostMapping("/searchAllComments")
    public R searchAllCommentsByPage(@RequestHeader("token") String token, @RequestBody PostCommentForm commentForm) {
        // 获取请求头中的token
        int start = commentForm.getStart();
        // 获取请求体中的start
        int length = commentForm.getLength();
        // 获取请求体中的length
        String postId = commentForm.getPostId();
        // 获取请求体中的postId
        start = (start - 1) * length;
        // 计算start
        HashMap hashMap = new HashMap();
        hashMap.put("start", start);
        hashMap.put("length", length);
        hashMap.put("postId", postId);
        // 创建hashMap
        List<HashMap> maps = postCommentService.searchAllComments(hashMap);
        // 调用searchAllComments方法查询数据
        return R.ok().put("result", maps);
        // 返回查询结果
    }

    @PostMapping("/addComment")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R addComment(@RequestHeader("token") String token, @RequestBody AddPostCommentForm addCommentForm) {
        // 获取请求头中的token
        String userId = jwtUtil.getUserId(token);
        // 调用jwtUtil.getUserId方法获取userId
        String postId = addCommentForm.getPostId();
        // 获取请求体中的postId
        String commentContent = addCommentForm.getCommentContent();
        // 获取请求体中的commentContent
        String parentId = addCommentForm.getParentId();
        // 获取请求体中的parentId
        String picture = addCommentForm.getPicture();
        // 获取请求体中的picture
        StringSnowflakeIdGenerator stringSnowflakeIdGenerator = new StringSnowflakeIdGenerator(1, 1);
        // 创建StringSnowflakeIdGenerator对象
        String id = stringSnowflakeIdGenerator.nextId();
        // 调用StringSnowflakeIdGenerator.nextId方法获取id
        HashMap hashMap = new HashMap();
        // 创建hashMap
        if (parentId != null && parentId != "") {
            ArrayList<String> arrayList = new ArrayList();
            arrayList.add(parentId);
            hashMap.put("parentId", arrayList.toString());
            // 判断parentId是否为空
            String beforeRootId = postCommentService.selectParentCommentById(parentId);
            HashMap map = new HashMap();
            // 创建HashMap对象
            if (beforeRootId == null || beforeRootId.equals("")) {
                ArrayList<String> strings = new ArrayList<>();
                strings.add(id);
                map.put("parentId", parentId);
                map.put("rootId", strings.toString());
                // 判断beforeRootId是否为空
                Integer result = postCommentService.addParentComment(map);
                // 调用postCommentService.addParentComment方法添加父评论
                if (result != 1) {
                    throw new CunionException("回复失败！请重试！");
                }
            } else {
                List<String> list = JSONUtil.toList(beforeRootId, String.class);
                list.add(id);
                map.put("rootId", list.toString());
                map.put("parentId", parentId);
                // 调用postCommentService.addParentComment方法添加父评论
                postCommentService.addParentComment(map);
            }
        }
        hashMap.put("userId", userId);
        hashMap.put("postId", postId);
        hashMap.put("commentContent", commentContent);
        hashMap.put("id", id);
        hashMap.put("picture", picture);
        // 添加评论
        postCommentService.addComment(hashMap);
        // 调用postCommentService.addComment方法添加评论
        return R.ok("评论成功！").put("result", id);
        // 返回添加评论成功信息
    }


    @PostMapping("/deleteMyComment")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R deleteMyComment(@RequestHeader("token") String token, @RequestBody DeleteMyPostCommentForm deleteMyCommentForm){
        // 获取请求头中的token
        String userId = jwtUtil.getUserId(token);
        // 调用jwtUtil.getUserId方法获取userId
        String commentId = deleteMyCommentForm.getCommentId();
        // 获取请求体中的commentId
        String postId = deleteMyCommentForm.getPostId();
        // 获取请求体中的postId
        HashMap map = new HashMap();
        map.put("userId", userId);
        map.put("id", commentId);
        map.put("postId", postId);
        // 创建hashMap
        Integer result = postCommentService.deleteMyComment(map);
        // 调用postCommentService.deleteMyComment方法删除评论
        return R.ok("删除评论成功！");
        // 返回删除评论成功信息
    }

    @GetMapping("/searchTopPostCommentById")
    public R searchTopPostCommentById(@RequestHeader("token") String token, @RequestParam("commentId") String commentId){
        // 获取请求头中的token
        HashMap map = postCommentService.searchTopCommentById(commentId);
        // 调用postCommentService.searchTopCommentById方法查询数据
        return R.ok().put("result", map);
        // 返回查询结果
    }
}
