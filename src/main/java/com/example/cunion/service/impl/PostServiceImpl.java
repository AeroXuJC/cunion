package com.example.cunion.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cunion.entity.Post;
import com.example.cunion.exception.CunionException;
import com.example.cunion.mapper.PostMapper;
import com.example.cunion.mapper.TagMapper;
import com.example.cunion.service.PostService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Aero
 * @description 针对表【post(帖子表)】的数据库操作Service实现
 * @createDate 2023-09-29 23:00:25
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {

    @Resource
    private PostMapper postMapper;
    @Resource
    private TagMapper tagMapper;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public List<HashMap> searchAllPosts(HashMap map) {
        long start = Long.parseLong(map.get("start").toString());
        long length = Long.parseLong(map.get("length").toString());
        String searchValue = map.get("searchValue").toString();
        long end = start + length - 1;
        Long size = redisTemplate.opsForList().size("post:searchAllPosts");
        if (size > 0 && searchValue.isEmpty()) {
            List range = redisTemplate.opsForList().range("post:searchAllPosts", start, end);
            return range;
        }
        ArrayList<HashMap> list = postMapper.searchAllPosts(map);
        ArrayList<HashMap> maps = postMapper.syncAllPosts(map);
        for (int i = 0; i < maps.size(); i++) {
            String tagList = maps.get(i).get("tagList").toString();
            HashMap hashMap = tagMapper.searchTagById(tagList);
            maps.get(i).replace("tagList", hashMap);
            if (maps.get(i).get("picture") != null && !"".equals(maps.get(i).get("picture"))) {
                String picture = maps.get(i).get("picture").toString();
                String[] split = picture.split(",");
                ArrayList arrayList = new ArrayList();
                for (int j = 0; j < split.length; j++) {
                    arrayList.add(split[j]);
                }
                maps.get(i).replace("picture", arrayList);
            }
        }

        for (int i = 0; i < list.size(); i++) {
            String tagList = list.get(i).get("tagList").toString();
            HashMap hashMap = tagMapper.searchTagById(tagList);
            list.get(i).replace("tagList", hashMap);
            if (list.get(i).get("picture") != null && !"".equals(list.get(i).get("picture"))) {
                String picture = list.get(i).get("picture").toString();
                String[] split = picture.split(",");
                ArrayList arrayList = new ArrayList();
                for (int j = 0; j < split.length; j++) {
                    arrayList.add(split[j]);
                }
                list.get(i).replace("picture", arrayList);
            }
        }
        if (searchValue.isEmpty()) {
            for (int i = maps.size() - 1; i >= 0; i--) {
                redisTemplate.opsForList().leftPush("post:searchAllPosts", maps.get(i));
            }
            redisTemplate.expire("post:searchAllPosts", 1, TimeUnit.HOURS);
        }
        return list;
    }

    @Override
    public HashMap searchAllPostById(HashMap map) {
        String id = map.get("id").toString();
        Object content = redisTemplate.opsForValue().get("post:content:" + id);
        if (content != null) {
            Map<String, Object> mapRedis = BeanUtil.beanToMap(content);
            return (HashMap) mapRedis;
        }
        HashMap hashMap = postMapper.searchAllPostById(map);
        if (hashMap == null){
            throw new CunionException("该帖已删除！");
        }
        if (hashMap.containsKey("picture")){
            Object picture = hashMap.get("picture");
            if (picture != null && !picture.equals("")) {
                ArrayList arrayList = new ArrayList();
                String[] split = picture.toString().split(",");
                for (int i = 0; i < split.length; i++) {
                    arrayList.add(split[i]);
                }
                hashMap.replace("picture", arrayList);
            }
        }
        String tagList = hashMap.get("tagList").toString();
        HashMap tagMap = tagMapper.searchTagById(tagList);
        hashMap.replace("tagList", tagMap);
        redisTemplate.opsForValue().set("post:content:" + id, hashMap);
        redisTemplate.expire("post:content:" + id, 1, TimeUnit.HOURS);
        return hashMap;
    }

    @Transactional
    @Override
    public Integer addPost(HashMap map) {
        String userId = map.get("userId").toString();
        redisTemplate.delete("post:myAllPost:" + userId);
        redisTemplate.delete("post:searchAllPosts");
        Set keys = redisTemplate.keys("post:class:*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        Integer result = postMapper.addPost(map);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new CunionException("添加失败");
        }
        redisTemplate.delete("post:myAllPost:" + userId);
        redisTemplate.delete("post:searchAllPosts");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        if (result != 1) {
            throw new CunionException("发帖失败！");
        }
        return result;
    }

    @Transactional
    @Override
    public Integer deletePost(String id, String userId) {
        redisTemplate.delete("post:myAllPost:" + userId);
        redisTemplate.delete("post:searchAllPosts");
        Set keys = redisTemplate.keys("post:class:*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        Integer result = postMapper.deletePost(id);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new CunionException("删除失败");
        }
        redisTemplate.delete("post:myAllPost:" + userId);
        redisTemplate.delete("post:searchAllPosts");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        if (result != 1) {
            throw new CunionException("帖子删除失败！");
        }
        return result;
    }

    @Override
    public List<HashMap> searchPostByTag(String classId) {
        if (classId.isEmpty()) {
            classId = "88888888";
        }
        Long size = redisTemplate.opsForList().size("post:class:" + classId);
        if (size > 0) {
            List range = redisTemplate.opsForList().range("post:class:" + classId, 0, -1);
            return range;
        }
        ArrayList<HashMap> list = new ArrayList<>();
        ArrayList<HashMap> arrayList = tagMapper.searchTagByClassId(classId);
        if (arrayList != null) {
            for (int i = 0; i < arrayList.size(); i++) {
                Object tagId = arrayList.get(i).get("id");
                ArrayList<HashMap> hashList = postMapper.searchPostByTag(tagId.toString());
                for (int j = 0; j < hashList.size(); j++) {
                    String tagList = hashList.get(j).get("tagList").toString();
                    HashMap hashMap = tagMapper.searchTagById(tagList);
                    hashList.get(j).replace("tagList", hashMap);
                    if (hashList.get(j).get("picture") != null && !"".equals(hashList.get(j).get("picture"))) {
                        String picture = hashList.get(j).get("picture").toString();
                        String[] split = picture.split(",");
                        ArrayList splitList = new ArrayList();
                        for (int k = 0; k < split.length; k++) {
                            splitList.add(split[k]);
                        }
                        hashList.get(j).replace("picture", splitList);
                    }
                    list.add(hashList.get(j));
                }
            }
        }
        for (int i = 0; i < list.size(); i++) {
            redisTemplate.opsForList().rightPush("post:class:" + classId, list.get(i));
        }
        redisTemplate.expire("post:class:" + classId, 1, TimeUnit.HOURS);
        return list;
    }

    @Override
    public List<HashMap> searchMyPost(HashMap map) {
        String userId = map.get("userId").toString();
        String searchValue = map.get("searchValue").toString();
        long start = Long.parseLong(map.get("start").toString());
        long length = Long.parseLong(map.get("length").toString());
        long end = start + length - 1;
        Long size = redisTemplate.opsForList().size("post:myAllPost:" + userId);
        if (searchValue.isEmpty()){
            if (size > 0){
                List range = redisTemplate.opsForList().range("post:myAllPost:" + userId, start, end);
                return range;
            }
            //同步mysql 与 redis
            List<HashMap> maps = postMapper.syncMyPost(map);
            for (int i = 0; i < maps.size(); i++) {
                String tagList = maps.get(i).get("tagList").toString();
                HashMap hashMap = tagMapper.searchTagById(tagList);
                maps.get(i).replace("tagList", hashMap);
                if (maps.get(i).get("picture") != null && !"".equals(maps.get(i).get("picture"))) {
                    String picture = maps.get(i).get("picture").toString();
                    String[] split = picture.split(",");
                    ArrayList arrayList = new ArrayList();
                    for (int j = 0; j < split.length; j++) {
                        arrayList.add(split[j]);
                    }
                    maps.get(i).replace("picture", arrayList);
                }
            }
            for (HashMap hashMap : maps){
                redisTemplate.opsForList().rightPush("post:myAllPost:" + userId, hashMap);
            }
            redisTemplate.expire("post:myAllPost:" + userId, 1, TimeUnit.HOURS);
        }
        List<HashMap> list = postMapper.searchMyPost(map);
        for (int i = 0; i < list.size(); i++) {
            String tagList = list.get(i).get("tagList").toString();
            HashMap hashMap = tagMapper.searchTagById(tagList);
            list.get(i).replace("tagList", hashMap);
            if (list.get(i).get("picture") != null && !"".equals(list.get(i).get("picture"))) {
                String picture = list.get(i).get("picture").toString();
                String[] split = picture.split(",");
                ArrayList arrayList = new ArrayList();
                for (int j = 0; j < split.length; j++) {
                    arrayList.add(split[j]);
                }
                list.get(i).replace("picture", arrayList);
            }
        }
        return list;
    }

    @Override
    public Integer searchMyPostNum(String userId) {
        Integer result = postMapper.searchMyPostNum(userId);
        return result;
    }
}




