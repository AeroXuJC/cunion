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
        PictureHandler pictureHandler = new PictureHandler();
        String postId = hashMap.get("postId").toString();
        long start = Long.parseLong(hashMap.get("start").toString());
        long length = Long.parseLong(hashMap.get("length").toString());
        long end = start + length - 1;
        Long size = redisTemplate.opsForList().size("comment:postComment:" + postId);
        if (size > 0) {
            List range = redisTemplate.opsForList().range("comment:postComment:" + postId, start, end);
            return range;
        }
        ArrayList<HashMap> maps = postCommentMapper.searchAllComments(hashMap);
        for (HashMap map : maps) {
            Object id = map.get("rootId");
            ArrayList<HashMap> list = pictureHandler.handle(map);
            map.replace("picture", list);
            if (id != null && !id.equals("")) {
                // 将字符串转换为JSONArray
                JSONArray jsonArray = new JSONArray(id.toString());
                ArrayList<HashMap> comments = new ArrayList<>();
                for (int i = jsonArray.size() - 1; i >= 0; i--) {
                    HashMap rootMap = postCommentMapper.searchCommentById(jsonArray.getStr(i).trim());
                    if (rootMap != null && !rootMap.equals("")) {
                        ArrayList<HashMap> rootList = pictureHandler.handle(rootMap);
                        rootMap.replace("picture", rootList);
                    }
                    comments.add(rootMap);
                }
                map.put("rootComment", comments);
            }
        }
        for (int i = 0; i < maps.size(); i++) {
            redisTemplate.opsForList().rightPush("comment:postComment:" + postId, maps.get(i));
        }
        redisTemplate.expire("comment:postComment:" + postId, 1, TimeUnit.HOURS);
        return maps;
    }

    @Override
    public Integer addComment(HashMap hashMap) {
        String postId = hashMap.get("postId").toString();
        redisTemplate.delete("comment:postComment:" + postId);
        Integer result = postCommentMapper.addComment(hashMap);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new CunionException("评论添加失败！");
        }
        redisTemplate.delete("comment:postComment:" + postId);
        if (result != 1) {
            throw new CunionException("评论失败");
        }
        return result;
    }

    @Override
    public String selectParentCommentById(String parentId) {
        Object o = redisTemplate.opsForValue().get("comment:postComment:" + parentId);
        if (o != null) {
            return o.toString();
        }
        String result = postCommentMapper.selectParentCommentById(parentId);

        return result;
    }

    @Override
    public Integer addParentComment(HashMap map) {
        String parentId = map.get("parentId").toString();
        redisTemplate.delete("comment:postComment:" + parentId);
        Integer result = postCommentMapper.addParentComment(map);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new CunionException("评论删除失败！");
        }
        redisTemplate.delete("comment:postComment:" + parentId);
        if (result != 1) {
            throw new CunionException("回复失败！请重试！");
        }
        return result;
    }

    @Override
    public Integer deleteMyComment(HashMap hashMap) {
        String postId = hashMap.get("postId").toString();
        String commentId = hashMap.get("id").toString();
        HashMap map = postCommentMapper.searchRootCommentById(commentId);
        if (map == null) {
            redisTemplate.delete("comment:postComment:" + postId);
            Integer result = postCommentMapper.deleteMyComment(hashMap);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                throw new CunionException("评论删除失败！");
            }
            redisTemplate.delete("comment:postComment:" + postId);
            if (result != 1) {
                throw new CunionException("删除评论失败");
            }
            return result;
        }
        JSONArray rootId = JSONUtil.parseArray(map.get("rootId"));
        List<String> parentId = new ArrayList();
        if (map.get("parentId") != null) {
            parentId = JSONUtil.toList(map.get("parentId").toString(), String.class);
        }
        Integer result = new Integer(0);
        for (int i = 0; i < rootId.size(); i++) {
            postCommentMapper.deleteCommentById(rootId.get(i).toString());
        }
        if (parentId != null && !parentId.equals("") && parentId.size() != 0) {
            String root = postCommentMapper.selectParentCommentById(parentId.get(0));
            List<String> list = JSONUtil.toList(root, String.class);
            list.remove(commentId);
            HashMap map1 = new HashMap();
            map1.put("parentId", parentId.get(0));
            map1.put("rootId", list.toString());
            postCommentMapper.updateParentRootId(map1);
        }
        redisTemplate.delete("comment:postComment:" + postId);
        result = postCommentMapper.deleteMyComment(hashMap);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new CunionException("评论删除失败！");
        }
        redisTemplate.delete("comment:postComment:" + postId);
        return result;
    }

    @Override
    public HashMap searchTopCommentById(String commentId) {
        HashMap map = postCommentMapper.searchCommentById(commentId);
        PictureHandler pictureHandler = new PictureHandler();
        try {
            ArrayList<HashMap> picture = pictureHandler.handle(map);
            map.put("picture", picture);
            Object rootId = map.get("rootId");
            ArrayList<HashMap> arrayList = new ArrayList<>();
            if (rootId != null && !"[]".equals(rootId.toString()) && !"".equals(rootId.toString())) {
                List<String> list = JSONUtil.toList(rootId.toString(), String.class);
                for (int i = list.size() - 1; i >= 0; i--) {
                    HashMap hashMap = postCommentMapper.searchCommentById(list.get(i));
                    if (hashMap != null && !hashMap.equals("")) {
                        ArrayList<HashMap> rootList = pictureHandler.handle(hashMap);
                        hashMap.replace("picture", rootList);
                    }
                    arrayList.add(hashMap);
                }
                map.put("rootComment", arrayList);
            }
        } catch (Exception e) {
            throw new CunionException("该评论已删除！");
        }
        return map;
    }
}




