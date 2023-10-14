package com.example.cunion.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cunion.entity.PostComment;
import com.example.cunion.exception.CunionException;
import com.example.cunion.patterns.PictureHandler;
import com.example.cunion.service.PostCommentService;
import com.example.cunion.mapper.PostCommentMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Aero
 * @description 针对表【post_comment(评论表)】的数据库操作Service实现
 * @createDate 2023-10-01 18:08:20
 */
@Service
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment>
        implements PostCommentService {

    @Resource
    private PostCommentMapper postCommentMapper;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public List<HashMap> searchAllComments(HashMap hashMap) {
        // 创建一个PictureHandler对象
        PictureHandler pictureHandler = new PictureHandler();
        // 获取postId
        String postId = hashMap.get("postId").toString();
        // 获取start
        long start = Long.parseLong(hashMap.get("start").toString());
        // 获取length
        long length = Long.parseLong(hashMap.get("length").toString());
        // 计算end
        long end = start + length - 1;
        // 获取评论列表长度
        Long size = redisTemplate.opsForList().size("comment:postComment:" + postId);
        // 如果评论列表长度大于0，则从redis中获取评论列表
        if (size > 0) {
            List range = redisTemplate.opsForList().range("comment:postComment:" + postId, start, end);
            return range;
        }
        // 从数据库中获取评论列表
        ArrayList<HashMap> maps = postCommentMapper.searchAllComments(hashMap);
        // 遍历评论列表
        for (HashMap map : maps) {
            // 获取rootId
            Object id = map.get("rootId");
            // 获取图片列表
            ArrayList<HashMap> list = pictureHandler.handle(map);
            // 将图片列表添加到map中
            map.replace("picture", list);
            // 如果rootId不为空，则从redis中获取rootComment
            if (id != null && !id.equals("")) {
                // 将字符串转换为JSONArray
                JSONArray jsonArray = new JSONArray(id.toString());
                ArrayList<HashMap> comments = new ArrayList<>();
                // 遍历JSONArray
                for (int i = jsonArray.size() - 1; i >= 0; i--) {
                    // 从数据库中获取rootComment
                    HashMap rootMap = postCommentMapper.searchCommentById(jsonArray.getStr(i).trim());
                    // 如果rootComment不为空，则获取图片列表
                    if (rootMap != null && !rootMap.equals("")) {
                        ArrayList<HashMap> rootList = pictureHandler.handle(rootMap);
                        rootMap.replace("picture", rootList);
                    }
                    // 将rootComment添加到comments中
                    comments.add(rootMap);
                }
                // 将comments添加到map中
                map.put("rootComment", comments);
            }
        }
        // 将map添加到redis中
        for (int i = 0; i < maps.size(); i++) {
            redisTemplate.opsForList().rightPush("comment:postComment:" + postId, maps.get(i));
        }
        // 设置redis过期时间
        redisTemplate.expire("comment:postComment:" + postId, 1, TimeUnit.HOURS);
        return maps;
    }

    @Override
    public Integer addComment(HashMap hashMap) {
        // 获取postId
        String postId = hashMap.get("postId").toString();
        // 删除redis中的评论列表
        redisTemplate.delete("comment:postComment:" + postId);
        // 向数据库中添加评论
        Integer result = postCommentMapper.addComment(hashMap);
        try {
            // 休眠150毫秒
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new CunionException("评论添加失败！");
        }
        // 删除redis中的评论列表
        redisTemplate.delete("comment:postComment:" + postId);
        // 如果添加失败，则抛出异常
        if (result != 1) {
            throw new CunionException("评论失败");
        }
        return result;
    }

    @Override
    public String selectParentCommentById(String parentId) {
        // 从redis中获取评论
        Object o = redisTemplate.opsForValue().get("comment:postComment:" + parentId);
        // 如果redis中有评论，则返回
        if (o != null) {
            return o.toString();
        }
        // 从数据库中获取评论
        String result = postCommentMapper.selectParentCommentById(parentId);

        return result;
    }

    @Override
    public Integer addParentComment(HashMap map) {
        // 获取parentId
        String parentId = map.get("parentId").toString();
        // 删除redis中的评论
        redisTemplate.delete("comment:postComment:" + parentId);
        // 向数据库中添加回复
        Integer result = postCommentMapper.addParentComment(map);
        try {
            // 休眠100毫秒
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new CunionException("评论删除失败！");
        }
        // 删除redis中的评论
        redisTemplate.delete("comment:postComment:" + parentId);
        // 如果添加失败，则抛出异常
        if (result != 1) {
            throw new CunionException("回复失败！请重试！");
        }
        return result;
    }

    @Override
    public Integer deleteMyComment(HashMap hashMap) {
        //获取评论id
        String postId = hashMap.get("postId").toString();
        String commentId = hashMap.get("id").toString();
        //根据评论id查询父评论
        HashMap map = postCommentMapper.searchRootCommentById(commentId);
        //如果查询到父评论
        if (map == null) {
            //删除redis中的评论
            redisTemplate.delete("comment:postComment:" + postId);
            //根据hashMap删除评论
            Integer result = postCommentMapper.deleteMyComment(hashMap);
            try {
                //休眠150毫秒
                Thread.sleep(150);
            } catch (InterruptedException e) {
                throw new CunionException("评论删除失败！");
            }
            //删除redis中的评论
            redisTemplate.delete("comment:postComment:" + postId);
            //如果删除失败
            if (result != 1) {
                throw new CunionException("删除评论失败");
            }
            return result;
        }
        //获取父评论id
        JSONArray rootId = JSONUtil.parseArray(map.get("rootId"));
        //获取父评论的父评论id
        List<String> parentId = new ArrayList();
        if (map.get("parentId") != null) {
            parentId = JSONUtil.toList(map.get("parentId").toString(), String.class);
        }
        Integer result = new Integer(0);
        //删除父评论
        for (int i = 0; i < rootId.size(); i++) {
            postCommentMapper.deleteCommentById(rootId.get(i).toString());
        }
        //如果父评论的父评论id不为空
        if (parentId != null && !parentId.equals("") && parentId.size() != 0) {
            //根据父评论id查询父评论
            String root = postCommentMapper.selectParentCommentById(parentId.get(0));
            //获取父评论的父评论id
            List<String> list = JSONUtil.toList(root, String.class);
            //移除当前评论id
            list.remove(commentId);
            //更新父评论的父评论id
            HashMap map1 = new HashMap();
            map1.put("parentId", parentId.get(0));
            map1.put("rootId", list.toString());
            postCommentMapper.updateParentRootId(map1);
        }
        //删除redis中的评论
        redisTemplate.delete("comment:postComment:" + postId);
        //根据hashMap删除评论
        result = postCommentMapper.deleteMyComment(hashMap);
        try {
            //休眠150毫秒
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new CunionException("评论删除失败！");
        }
        //删除redis中的评论
        redisTemplate.delete("comment:postComment:" + postId);
        return result;
    }

    @Override
    public HashMap searchTopCommentById(String commentId) {
        //根据评论id查询评论
        HashMap map = postCommentMapper.searchCommentById(commentId);
        //实例化PictureHandler
        PictureHandler pictureHandler = new PictureHandler();
        try {
            //处理图片
            ArrayList<HashMap> picture = pictureHandler.handle(map);
            //将图片放入map中
            map.put("picture", picture);
            //获取父评论id
            Object rootId = map.get("rootId");
            //实例化ArrayList
            ArrayList<HashMap> arrayList = new ArrayList<>();
            //如果查询到父评论
            if (rootId != null && !"[]".equals(rootId.toString()) && !"".equals(rootId.toString())) {
                //将父评论id转换为list
                List<String> list = JSONUtil.toList(rootId.toString(), String.class);
                //从最后一个开始遍历
                for (int i = list.size() - 1; i >= 0; i--) {
                    //根据父评论id查询评论
                    HashMap hashMap = postCommentMapper.searchCommentById(list.get(i));
                    //如果查询到评论
                    if (hashMap != null && !hashMap.equals("")) {
                        //处理图片
                        ArrayList<HashMap> rootList = pictureHandler.handle(hashMap);
                        //将图片放入hashMap中
                        hashMap.replace("picture", rootList);
                    }
                    //将hashMap放入ArrayList中
                    arrayList.add(hashMap);
                }
                //将ArrayList放入map中
                map.put("rootComment", arrayList);
            }
        } catch (Exception e) {
            throw new CunionException("该评论已删除！");
        }
        return map;
    }
}




