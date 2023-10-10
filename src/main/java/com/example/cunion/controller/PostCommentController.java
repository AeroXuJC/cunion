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
        int start = commentForm.getStart();
        int length = commentForm.getLength();
        String postId = commentForm.getPostId();
        start = (start - 1) * length;
        HashMap hashMap = new HashMap();
        hashMap.put("start", start);
        hashMap.put("length", length);
        hashMap.put("postId", postId);
        List<HashMap> maps = postCommentService.searchAllComments(hashMap);
        return R.ok().put("result", maps);
    }

    @PostMapping("/addComment")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R addComment(@RequestHeader("token") String token, @RequestBody AddPostCommentForm addCommentForm) {
        String userId = jwtUtil.getUserId(token);
        String postId = addCommentForm.getPostId();
        String commentContent = addCommentForm.getCommentContent();
        String parentId = addCommentForm.getParentId();
        String picture = addCommentForm.getPicture();
        StringSnowflakeIdGenerator stringSnowflakeIdGenerator = new StringSnowflakeIdGenerator(1, 1);
        String id = stringSnowflakeIdGenerator.nextId();
        HashMap hashMap = new HashMap();
        if (parentId != null && parentId != "") {
            ArrayList<String> arrayList = new ArrayList();
            arrayList.add(parentId);
            hashMap.put("parentId", arrayList.toString());
            String beforeRootId = postCommentService.selectParentCommentById(parentId);
            HashMap map = new HashMap();
            if (beforeRootId == null || beforeRootId.equals("")) {
                ArrayList<String> strings = new ArrayList<>();
                strings.add(id);
                map.put("parentId", parentId);
                map.put("rootId", strings.toString());
                Integer result = postCommentService.addParentComment(map);
                if (result != 1) {
                    throw new CunionException("回复失败！请重试！");
                }
            } else {
                List<String> list = JSONUtil.toList(beforeRootId, String.class);
                list.add(id);
                map.put("rootId", list.toString());
                map.put("parentId", parentId);
                postCommentService.addParentComment(map);
            }
        }
        hashMap.put("userId", userId);
        hashMap.put("postId", postId);
        hashMap.put("commentContent", commentContent);
        hashMap.put("id", id);
        hashMap.put("picture", picture);
        postCommentService.addComment(hashMap);
        return R.ok("评论成功！").put("result", id);
    }


    @PostMapping("/deleteMyComment")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R deleteMyComment(@RequestHeader("token") String token, @RequestBody DeleteMyPostCommentForm deleteMyCommentForm){
        String userId = jwtUtil.getUserId(token);
        String commentId = deleteMyCommentForm.getCommentId();
        String postId = deleteMyCommentForm.getPostId();
        HashMap map = new HashMap();
        map.put("userId", userId);
        map.put("id", commentId);
        map.put("postId", postId);
        Integer result = postCommentService.deleteMyComment(map);
        return R.ok("删除评论成功！");
    }

    @GetMapping("/searchTopPostCommentById")
    public R searchTopPostCommentById(@RequestHeader("token") String token, @RequestParam("commentId") String commentId){
        HashMap map = postCommentService.searchTopCommentById(commentId);
        return R.ok().put("result", map);
    }
}
