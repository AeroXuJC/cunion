package com.example.cunion.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.cunion.exception.CunionException;
import com.example.cunion.mapper.CommentMapper;
import com.example.cunion.patterns.PictureHandler;
import com.example.cunion.service.CommentService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public List<HashMap> searchAllComments(HashMap hashMap) {
        PictureHandler pictureHandler = new PictureHandler();
        long start = Long.parseLong(hashMap.get("start").toString());
        long length = Long.parseLong(hashMap.get("length").toString());
        String shopId = hashMap.get("shopId").toString();
        long end = start + length - 1;
        Long size = redisTemplate.opsForList().size("comment:searchAllComments:" + shopId);
        if (size > 0) {
            List range = redisTemplate.opsForList().range("comment:searchAllComments:" + shopId, start, end);
            return range;
        }
        ArrayList<HashMap> maps = commentMapper.searchAllComments(hashMap);
        for (HashMap map : maps) {
            Object id = map.get("rootId");
            ArrayList<HashMap> list = pictureHandler.handle(map);
            map.replace("picture", list);
            if (id != null && !id.equals("")) {
                // 将字符串转换为JSONArray
                JSONArray jsonArray = new JSONArray(id.toString());
                ArrayList<HashMap> comments = new ArrayList<>();
                for (int i = jsonArray.size() - 1; i >= 0; i--) {
                    HashMap rootMap = commentMapper.searchCommentById(jsonArray.getStr(i).trim());
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
            redisTemplate.opsForList().rightPush("comment:searchAllComments:" + shopId, maps.get(i));
        }
        redisTemplate.expire("comment:searchAllComments:" + shopId, 1, TimeUnit.HOURS);
        return maps;
    }

    @Transactional
    @Override
    public Integer addComment(HashMap hashMap) {
        String shopId = hashMap.get("shopId").toString();
        redisTemplate.delete("comment:searchAllComments:" + shopId);
        Integer result = commentMapper.addComment(hashMap);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new CunionException("评论添加失败！");
        }
        redisTemplate.delete("comment:searchAllComments:" + shopId);
        if (result != 1) {
            throw new CunionException("评论失败");
        }
        return result;
    }

    @Override
    public String selectParentCommentById(String parentId) {
        Object o = redisTemplate.opsForValue().get("post:parentComment:" + parentId);
        if (o != null) {
            return o.toString();
        }
        String result = commentMapper.selectParentCommentById(parentId);
        return result;
    }

    @Transactional
    @Override
    public Integer addParentComment(HashMap map) {
        String parentId = map.get("parentId").toString();
        redisTemplate.delete("post:parentComment:" + parentId);
        Integer result = commentMapper.addParentComment(map);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new CunionException("添加评论失败！");
        }
        redisTemplate.delete("post:parentComment:" + parentId);
        if (result != 1) {
            throw new CunionException("回复失败！请重试！");
        }
        return result;
    }

    @Override
    public Integer deleteMyComment(HashMap hashMap) {
        String commentId = hashMap.get("id").toString();
        HashMap map = commentMapper.searchRootCommentById(commentId);
        if (map == null) {
            Set keys = redisTemplate.keys("comment:searchAllComments:*");
            if (keys != null) {
                redisTemplate.delete(keys);
            }
            Integer result = commentMapper.deleteMyComment(hashMap);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                throw new CunionException("删除评论失败！");
            }
            if (keys != null) {
                redisTemplate.delete(keys);
            }
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
            result = commentMapper.deleteCommentById(rootId.get(i).toString());
        }
        if (parentId != null && !parentId.equals("") && parentId.size() != 0) {
            String root = commentMapper.selectParentCommentById(parentId.get(0));
            List<String> list = JSONUtil.toList(root, String.class);
            list.remove(commentId);
            HashMap map1 = new HashMap();
            map1.put("parentId", parentId.get(0));
            map1.put("rootId", list.toString());
            commentMapper.updateParentRootId(map1);
        }
        Set keys = redisTemplate.keys("comment:searchAllComments:*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        commentMapper.deleteMyComment(hashMap);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new CunionException("删除评论失败！");
        }
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        return result;
    }

    @Override
    public HashMap searchTopCommentById(String commentId) {
        HashMap map = commentMapper.searchCommentById(commentId);
        PictureHandler pictureHandler = new PictureHandler();
        try {
            ArrayList<HashMap> picture = pictureHandler.handle(map);
            map.put("picture", picture);
            Object rootId = map.get("rootId");
            ArrayList<HashMap> arrayList = new ArrayList<>();
            if (rootId != null && !"[]".equals(rootId.toString()) && !"".equals(rootId.toString())) {
                List<String> list = JSONUtil.toList(rootId.toString(), String.class);
                for (int i = list.size() - 1; i >= 0; i--) {
                    HashMap hashMap = commentMapper.searchCommentById(list.get(i));
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
