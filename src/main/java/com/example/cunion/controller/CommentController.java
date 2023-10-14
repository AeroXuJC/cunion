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
        //获取分页信息
        int start = commentForm.getStart();
        int length = commentForm.getLength();
        //获取shopId
        String shopId = commentForm.getShopId();
        //计算起始位置
        start = (start - 1) * length;
        //创建HashMap
        HashMap hashMap = new HashMap();
        hashMap.put("start", start);
        hashMap.put("length", length);
        hashMap.put("shopId", shopId);
        //调用commentService的searchAllComments方法
        List<HashMap> maps = commentService.searchAllComments(hashMap);
        //返回结果
        return R.ok().put("result", maps);
    }

    @PostMapping("/addComment")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R addComment(@RequestHeader("token") String token, @RequestBody AddCommentForm addCommentForm) {
        //获取用户id
        String userId = jwtUtil.getUserId(token);
        //获取评论内容
        String shopId = addCommentForm.getShopId();
        String commentContent = addCommentForm.getCommentContent();
        //获取父评论id
        String parentId = addCommentForm.getParentId();
        //获取图片
        String picture = addCommentForm.getPicture();
        //生成id
        StringSnowflakeIdGenerator stringSnowflakeIdGenerator = new StringSnowflakeIdGenerator(1, 1);
        String id = stringSnowflakeIdGenerator.nextId();
        //创建map
        HashMap hashMap = new HashMap();
        //判断是否有父评论
        if (parentId != null && parentId != "") {
            //创建list
            ArrayList<String> arrayList = new ArrayList();
            //将父评论id添加到list中
            arrayList.add(parentId);
            //将父评论id添加到map中
            hashMap.put("parentId", arrayList.toString());
            //获取父评论id
            String beforeRootId = commentService.selectParentCommentById(parentId);
            //创建map
            HashMap map = new HashMap();
            //判断父评论id是否为空
            if (beforeRootId == null || beforeRootId.equals("")) {
                //创建list
                ArrayList<String> strings = new ArrayList<>();
                //将id添加到list中
                strings.add(id);
                //将父评论id添加到map中
                map.put("parentId", parentId);
                map.put("rootId", strings.toString());
                //调用服务层
                Integer result = commentService.addParentComment(map);
                //判断返回结果
                if (result != 1) {
                    throw new CunionException("回复失败！请重试！");
                }
            } else {
                //将父评论id转换为list
                List<String> list = JSONUtil.toList(beforeRootId, String.class);
                //将id添加到list中
                list.add(id);
                //将父评论id添加到map中
                map.put("rootId", list.toString());
                map.put("parentId", parentId);
                //调用服务层
                commentService.addParentComment(map);
            }
        }
        //将用户id、评论内容、id、图片添加到map中
        hashMap.put("userId", userId);
        hashMap.put("shopId", shopId);
        hashMap.put("commentContent", commentContent);
        hashMap.put("id", id);
        hashMap.put("picture", picture);
        //调用服务层
        commentService.addComment(hashMap);
        //返回结果
        return R.ok("评论成功！");
    }


    @PostMapping("/deleteMyComment")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R deleteMyComment(@RequestHeader("token") String token, @RequestBody DeleteMyCommentForm deleteMyCommentForm){
        //获取用户id
        String userId = jwtUtil.getUserId(token);
        //获取评论id
        String commentId = deleteMyCommentForm.getCommentId();
        //创建map
        HashMap map = new HashMap();
        //将用户id和评论id放入map
        map.put("userId", userId);
        map.put("id", commentId);
        //调用删除评论的方法
        Integer result = commentService.deleteMyComment(map);
        //返回结果
        return R.ok("删除评论成功！");
    }

    @GetMapping("/searchTopCommentById")
    //根据评论id搜索评论
    public R searchTopCommentById(@RequestHeader("token") String token, @RequestParam("commentId") String commentId){
        //调用搜索评论的方法
        HashMap map = commentService.searchTopCommentById(commentId);
        //返回结果
        return R.ok().put("result", map);
    }
}
