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
        // 创建一个PictureHandler对象
        PictureHandler pictureHandler = new PictureHandler();
        // 获取开始位置
        long start = Long.parseLong(hashMap.get("start").toString());
        // 获取长度
        long length = Long.parseLong(hashMap.get("length").toString());
        // 获取shopId
        String shopId = hashMap.get("shopId").toString();
        // 计算结束位置
        long end = start + length - 1;
        // 获取评论列表长度
        Long size = redisTemplate.opsForList().size("comment:searchAllComments:" + shopId);
        // 如果评论列表长度大于0，则从redis中获取评论列表
        if (size > 0) {
            List range = redisTemplate.opsForList().range("comment:searchAllComments:" + shopId, start, end);
            return range;
        }
        // 从数据库中获取评论列表
        ArrayList<HashMap> maps = commentMapper.searchAllComments(hashMap);
        // 遍历评论列表，处理图片
        for (HashMap map : maps) {
            Object id = map.get("rootId");
            ArrayList<HashMap> list = pictureHandler.handle(map);
            map.replace("picture", list);
            // 如果rootId不为空，则从数据库中获取rootId对应的评论列表
            if (id != null && !id.equals("")) {
                // 将字符串转换为JSONArray
                JSONArray jsonArray = new JSONArray(id.toString());
                ArrayList<HashMap> comments = new ArrayList<>();
                // 遍历JSONArray，从数据库中获取rootId对应的评论
                for (int i = jsonArray.size() - 1; i >= 0; i--) {
                    HashMap rootMap = commentMapper.searchCommentById(jsonArray.getStr(i).trim());
                    // 如果rootId对应的评论不为空，则处理图片
                    if (rootMap != null && !rootMap.equals("")) {
                        ArrayList<HashMap> rootList = pictureHandler.handle(rootMap);
                        rootMap.replace("picture", rootList);
                    }
                    comments.add(rootMap);
                }
                map.put("rootComment", comments);
            }
        }
        // 将处理后的评论列表放入redis
        for (int i = 0; i < maps.size(); i++) {
            redisTemplate.opsForList().rightPush("comment:searchAllComments:" + shopId, maps.get(i));
        }
        // 设置redis过期时间
        redisTemplate.expire("comment:searchAllComments:" + shopId, 1, TimeUnit.HOURS);
        return maps;
    }

   @Transactional
    @Override
    public Integer addComment(HashMap hashMap) {
        //从hashMap中获取shopId
        String shopId = hashMap.get("shopId").toString();
        //删除redis中的comment:searchAllComments:shopId
        redisTemplate.delete("comment:searchAllComments:" + shopId);
        //调用commentMapper的addComment方法，添加评论
        Integer result = commentMapper.addComment(hashMap);
        try {
            //线程休眠150毫秒
            Thread.sleep(150);
        } catch (InterruptedException e) {
            //抛出异常
            throw new CunionException("评论添加失败！");
        }
        //删除redis中的comment:searchAllComments:shopId
        redisTemplate.delete("comment:searchAllComments:" + shopId);
        //如果添加失败，抛出异常
        if (result != 1) {
            throw new CunionException("评论失败");
        }
        //返回添加结果
        return result;
    }

    @Override
    public String selectParentCommentById(String parentId) {
        //从redis中获取parentId对应的父评论
        Object o = redisTemplate.opsForValue().get("post:parentComment:" + parentId);
        if (o != null) {
            return o.toString();
        }
        //从数据库中获取parentId对应的父评论
        String result = commentMapper.selectParentCommentById(parentId);
        return result;
    }

    @Transactional
    @Override
    public Integer addParentComment(HashMap map) {
        //从map中获取父评论id
        String parentId = map.get("parentId").toString();
        //从redis中删除父评论
        redisTemplate.delete("post:parentComment:" + parentId);
        //调用commentMapper中的addParentComment方法添加父评论
        Integer result = commentMapper.addParentComment(map);
        try {
            //线程休眠100毫秒
            Thread.sleep(500);
        } catch (InterruptedException e) {
            //抛出异常
            throw new CunionException("添加评论失败！");
        }
        //从redis中删除父评论
        redisTemplate.delete("post:parentComment:" + parentId);
        //判断添加父评论是否成功
        if (result != 1) {
            throw new CunionException("回复失败！请重试！");
        }
        //返回添加父评论的结果
        return result;
    }

   @Override
    public Integer deleteMyComment(HashMap hashMap) {
        //获取评论id
        String commentId = hashMap.get("id").toString();
        //根据评论id查询父评论
        HashMap map = commentMapper.searchRootCommentById(commentId);
        //如果查询到父评论
        if (map == null) {
            //获取所有评论的key
            Set keys = redisTemplate.keys("comment:searchAllComments:*");
            //如果key不为空，则删除
            if (keys != null) {
                redisTemplate.delete(keys);
            }
            //根据hashmap删除评论
            Integer result = commentMapper.deleteMyComment(hashMap);
            try {
                //休眠150毫秒
                Thread.sleep(150);
            } catch (InterruptedException e) {
                throw new CunionException("删除评论失败！");
            }
            //如果key不为空，则删除
            if (keys != null) {
                redisTemplate.delete(keys);
            }
            //如果删除失败，则抛出异常
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
        //根据父评论id删除评论
        for (int i = 0; i < rootId.size(); i++) {
            result = commentMapper.deleteCommentById(rootId.get(i).toString());
        }
        //如果父评论id不为空，则更新父评论的父评论id
        if (parentId != null && !parentId.equals("") && parentId.size() != 0) {
            String root = commentMapper.selectParentCommentById(parentId.get(0));
            List<String> list = JSONUtil.toList(root, String.class);
            list.remove(commentId);
            HashMap map1 = new HashMap();
            map1.put("parentId", parentId.get(0));
            map1.put("rootId", list.toString());
            commentMapper.updateParentRootId(map1);
        }
        //获取所有评论的key
        Set keys = redisTemplate.keys("comment:searchAllComments:*");
        //如果key不为空，则删除
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        //根据hashmap删除评论
        commentMapper.deleteMyComment(hashMap);
        try {
            //休眠150毫秒
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new CunionException("删除评论失败！");
        }
        //如果key不为空，则删除
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        return result;
    }

    @Override
    public HashMap searchTopCommentById(String commentId) {
        // 根据评论id查询评论信息
        HashMap map = commentMapper.searchCommentById(commentId);
        // 创建图片处理器
        PictureHandler pictureHandler = new PictureHandler();
        try {
            // 处理图片
            ArrayList<HashMap> picture = pictureHandler.handle(map);
            // 将图片添加到map中
            map.put("picture", picture);
            // 获取根评论id
            Object rootId = map.get("rootId");
            ArrayList<HashMap> arrayList = new ArrayList<>();
            // 如果根评论id不为空，则查询根评论
            if (rootId != null && !"[]".equals(rootId.toString()) && !"".equals(rootId.toString())) {
                // 将根评论id转换为list
                List<String> list = JSONUtil.toList(rootId.toString(), String.class);
                // 从最后一个开始查询
                for (int i = list.size() - 1; i >= 0; i--) {
                    // 根据根评论id查询评论信息
                    HashMap hashMap = commentMapper.searchCommentById(list.get(i));
                    // 如果查询到评论信息，则处理图片
                    if (hashMap != null && !hashMap.equals("")) {
                        ArrayList<HashMap> rootList = pictureHandler.handle(hashMap);
                        // 将图片添加到map中
                        hashMap.replace("picture", rootList);
                    }
                    // 将查询到的评论信息添加到list中
                    arrayList.add(hashMap);
                }
                // 将根评论list添加到map中
                map.put("rootComment", arrayList);
            }
        } catch (Exception e) {
            // 如果抛出异常，则抛出CunionException异常
            throw new CunionException("该评论已删除！");
        }
        return map;
    }
}
