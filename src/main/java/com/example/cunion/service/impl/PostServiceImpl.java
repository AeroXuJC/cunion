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
        // 获取map中的start和length
        long start = Long.parseLong(map.get("start").toString());
        long length = Long.parseLong(map.get("length").toString());
        // 获取map中的searchValue
        String searchValue = map.get("searchValue").toString();
        // 计算end
        long end = start + length - 1;
        // 获取redis中post:searchAllPosts的size
        Long size = redisTemplate.opsForList().size("post:searchAllPosts");
        // 如果size大于0且searchValue为空，则从redis中获取range
        if (size > 0 && searchValue.isEmpty()) {
            List range = redisTemplate.opsForList().range("post:searchAllPosts", start, end);
            return range;
        }
        // 从数据库中获取list
        ArrayList<HashMap> list = postMapper.searchAllPosts(map);
        // 从数据库中获取maps
        ArrayList<HashMap> maps = postMapper.syncAllPosts(map);
        // 遍历maps，从tagMapper中获取tagList，并将picture转换为arrayList
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

        // 遍历list，从tagMapper中获取tagList，并将picture转换为arrayList
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
        // 如果searchValue为空，则将maps中的元素添加到redis中
        if (searchValue.isEmpty()) {
            for (int i = maps.size() - 1; i >= 0; i--) {
                redisTemplate.opsForList().leftPush("post:searchAllPosts", maps.get(i));
            }
            // 设置redis的过期时间
            redisTemplate.expire("post:searchAllPosts", 1, TimeUnit.HOURS);
        }
        // 返回list
        return list;
    }

    @Override
    public HashMap searchAllPostById(HashMap map) {
        //根据id查询帖子
        String id = map.get("id").toString();
        //根据id查询帖子
        HashMap hashMap = postMapper.searchAllPostById(map);
        //如果查询结果为空，抛出异常
        if (hashMap == null){
            throw new CunionException("该帖已删除！");
        }
        //如果查询结果中包含图片，则将图片转换为ArrayList
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
        //如果查询结果中包含标签，则根据标签id查询标签
        String tagList = hashMap.get("tagList").toString();
        HashMap tagMap = tagMapper.searchTagById(tagList);
        hashMap.replace("tagList", tagMap);
        //返回查询结果
        return hashMap;
    }

    @Transactional
    @Override
    public Integer addPost(HashMap map) {
        //根据map中的userId查询用户
        String userId = map.get("userId").toString();
        //根据userId删除redis中的帖子
        redisTemplate.delete("post:myAllPost:" + userId);
        //根据userId和搜索条件删除redis中的帖子
        redisTemplate.delete("post:searchAllPosts");
        //根据classId删除redis中的帖子
        Set keys = redisTemplate.keys("post:class:*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        //根据map中的参数添加帖子
        Integer result = postMapper.addPost(map);
        try {
            //线程休眠150毫秒
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new CunionException("添加失败");
        }
        //根据userId删除redis中的帖子
        redisTemplate.delete("post:myAllPost:" + userId);
        //根据userId和搜索条件删除redis中的帖子
        redisTemplate.delete("post:searchAllPosts");
        //根据classId删除redis中的帖子
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        //如果添加帖子失败，抛出异常
        if (result != 1) {
            throw new CunionException("发帖失败！");
        }
        //返回添加帖子的结果
        return result;
    }

    @Transactional
    @Override
    public Integer deletePost(String id, String userId) {
        //根据userId删除redis中的帖子
        redisTemplate.delete("post:myAllPost:" + userId);
        //根据userId和搜索条件删除redis中的帖子
        redisTemplate.delete("post:searchAllPosts");
        //根据classId删除redis中的帖子
        Set keys = redisTemplate.keys("post:class:*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        //根据id删除帖子
        Integer result = postMapper.deletePost(id);
        try {
            //线程休眠150毫秒
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new CunionException("删除失败");
        }
        //根据userId删除redis中的帖子
        redisTemplate.delete("post:myAllPost:" + userId);
        //根据userId和搜索条件删除redis中的帖子
        redisTemplate.delete("post:searchAllPosts");
        //根据classId删除redis中的帖子
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        //如果删除帖子失败，抛出异常
        if (result != 1) {
            throw new CunionException("帖子删除失败！");
        }
        //返回删除帖子的结果
        return result;
    }

    @Override
    public List<HashMap> searchPostByTag(String classId) {
        //如果classId为空，则设置默认值
        if (classId.isEmpty()) {
            classId = "88888888";
        }
        //获取classId对应的列表长度
        Long size = redisTemplate.opsForList().size("post:class:" + classId);
        //如果列表长度大于0，则从redis中获取列表
        if (size > 0) {
            List range = redisTemplate.opsForList().range("post:class:" + classId, 0, -1);
            return range;
        }
        //创建一个ArrayList，用于存放查询结果
        ArrayList<HashMap> list = new ArrayList<>();
        //根据classId查询标签
        ArrayList<HashMap> arrayList = tagMapper.searchTagByClassId(classId);
        //如果查询结果不为空，则遍历查询结果
        if (arrayList != null) {
            for (int i = 0; i < arrayList.size(); i++) {
                //获取标签id
                Object tagId = arrayList.get(i).get("id");
                //根据标签id查询文章
                ArrayList<HashMap> hashList = postMapper.searchPostByTag(tagId.toString());
                //遍历查询结果
                for (int j = 0; j < hashList.size(); j++) {
                    //获取标签列表
                    String tagList = hashList.get(j).get("tagList").toString();
                    //根据标签列表查询标签
                    HashMap hashMap = tagMapper.searchTagById(tagList);
                    //将查询结果替换到查询结果中
                    hashList.get(j).replace("tagList", hashMap);
                    //如果查询结果中的图片不为空，则将图片拆分成数组
                    if (hashList.get(j).get("picture") != null && !"".equals(hashList.get(j).get("picture"))) {
                        String picture = hashList.get(j).get("picture").toString();
                        String[] split = picture.split(",");
                        ArrayList splitList = new ArrayList();
                        for (int k = 0; k < split.length; k++) {
                            splitList.add(split[k]);
                        }
                        hashList.get(j).replace("picture", splitList);
                    }
                    //将查询结果添加到ArrayList中
                    list.add(hashList.get(j));
                }
            }
        }
        //将查询结果添加到redis中
        for (int i = 0; i < list.size(); i++) {
            redisTemplate.opsForList().rightPush("post:class:" + classId, list.get(i));
        }
        //设置redis过期时间
        redisTemplate.expire("post:class:" + classId, 1, TimeUnit.HOURS);
        //返回查询结果
        return list;
    }

    @Override
    public List<HashMap> searchMyPost(HashMap map) {
        //获取用户id
        String userId = map.get("userId").toString();
        //获取搜索值
        String searchValue = map.get("searchValue").toString();
        //获取开始位置
        long start = Long.parseLong(map.get("start").toString());
        //获取每页数量
        long length = Long.parseLong(map.get("length").toString());
        //获取结束位置
        long end = start + length - 1;
        //获取当前用户的所有文章列表
        Long size = redisTemplate.opsForList().size("post:myAllPost:" + userId);
        //如果搜索值为空
        if (searchValue.isEmpty()){
            //如果当前用户的所有文章列表大于0，则从redis中获取列表
            if (size > 0){
                List range = redisTemplate.opsForList().range("post:myAllPost:" + userId, start, end);
                return range;
            }
            //同步mysql 与 redis
            List<HashMap> maps = postMapper.syncMyPost(map);
            //遍历查询结果
            for (int i = 0; i < maps.size(); i++) {
                //获取标签列表
                String tagList = maps.get(i).get("tagList").toString();
                //根据标签列表查询标签
                HashMap hashMap = tagMapper.searchTagById(tagList);
                //将查询结果替换到查询结果中
                maps.get(i).replace("tagList", hashMap);
                //如果查询结果中的图片不为空，则将图片拆分成数组
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
            //将查询结果添加到redis中
            for (HashMap hashMap : maps){
                redisTemplate.opsForList().rightPush("post:myAllPost:" + userId, hashMap);
            }
            //设置redis过期时间
            redisTemplate.expire("post:myAllPost:" + userId, 1, TimeUnit.HOURS);
        }
        //根据用户id查询文章
        List<HashMap> list = postMapper.searchMyPost(map);
        //遍历查询结果
        for (int i = 0; i < list.size(); i++) {
            //获取标签列表
            String tagList = list.get(i).get("tagList").toString();
            //根据标签列表查询标签
            HashMap hashMap = tagMapper.searchTagById(tagList);
            //将查询结果替换到查询结果中
            list.get(i).replace("tagList", hashMap);
            //如果查询结果中的图片不为空，则将图片拆分成数组
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
        //返回查询结果
        return list;
    }

    @Override
    public Integer searchMyPostNum(String userId) {
        //根据用户id查询文章数量
        Integer result = postMapper.searchMyPostNum(userId);
        return result;
    }
}




