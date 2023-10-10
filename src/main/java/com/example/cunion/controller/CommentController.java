package com.example.cunion.controller;

import cn.hutool.json.JSONUtil;
import com.example.cunion.common.R;
import com.example.cunion.config.shiro.JwtUtil;
import com.example.cunion.controller.form.AddCommentForm;
import com.example.cunion.controller.form.CommentForm;
import com.example.cunion.controller.form.DeleteMyCommentForm;
import com.example.cunion.exception.CunionException;
import com.example.cunion.service.CommentService;
import com.example.cunion.util.StringSnowflakeIdGenerator;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Resource
    private CommentService commentService;

    @Resource
    private JwtUtil jwtUtil;

    @PostMapping("/searchAllComments")
    public R searchAllCommentsByPage(@RequestHeader("token") String token, @RequestBody CommentForm commentForm) {
        int start = commentForm.getStart();
        int length = commentForm.getLength();
        String shopId = commentForm.getShopId();
        start = (start - 1) * length;
        HashMap hashMap = new HashMap();
        hashMap.put("start", start);
        hashMap.put("length", length);
        hashMap.put("shopId", shopId);
        List<HashMap> maps = commentService.searchAllComments(hashMap);
        return R.ok().put("result", maps);
    }

    @PostMapping("/addComment")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R addComment(@RequestHeader("token") String token, @RequestBody AddCommentForm addCommentForm) {
        String userId = jwtUtil.getUserId(token);
        String shopId = addCommentForm.getShopId();
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
            String beforeRootId = commentService.selectParentCommentById(parentId);
            HashMap map = new HashMap();
            if (beforeRootId == null || beforeRootId.equals("")) {
                ArrayList<String> strings = new ArrayList<>();
                strings.add(id);
                map.put("parentId", parentId);
                map.put("rootId", strings.toString());
                Integer result = commentService.addParentComment(map);
                if (result != 1) {
                    throw new CunionException("回复失败！请重试！");
                }
            } else {
                List<String> list = JSONUtil.toList(beforeRootId, String.class);
                list.add(id);
                map.put("rootId", list.toString());
                map.put("parentId", parentId);
                commentService.addParentComment(map);
            }
        }
        hashMap.put("userId", userId);
        hashMap.put("shopId", shopId);
        hashMap.put("commentContent", commentContent);
        hashMap.put("id", id);
        hashMap.put("picture", picture);
        commentService.addComment(hashMap);
        return R.ok("评论成功！");
    }


    @PostMapping("/deleteMyComment")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R deleteMyComment(@RequestHeader("token") String token, @RequestBody DeleteMyCommentForm deleteMyCommentForm){
        String userId = jwtUtil.getUserId(token);
        String commentId = deleteMyCommentForm.getCommentId();
        HashMap map = new HashMap();
        map.put("userId", userId);
        map.put("id", commentId);
        Integer result = commentService.deleteMyComment(map);
        return R.ok("删除评论成功！");
    }

    @GetMapping("/searchTopCommentById")
    public R searchTopCommentById(@RequestHeader("token") String token, @RequestParam("commentId") String commentId){
        HashMap map = commentService.searchTopCommentById(commentId);
        return R.ok().put("result", map);
    }
}
