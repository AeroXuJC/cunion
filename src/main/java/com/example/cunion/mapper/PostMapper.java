package com.example.cunion.mapper;

import com.example.cunion.entity.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Aero
* @description 针对表【post(帖子表)】的数据库操作Mapper
* @createDate 2023-09-29 23:00:25
* @Entity com.example.cunion.entity.Post
*/
public interface PostMapper extends BaseMapper<Post> {

    ArrayList<HashMap> searchAllPosts(HashMap map);

    HashMap searchAllPostById(HashMap map);

    String searchThumbListById(String postId);

    Integer updateThumbList(HashMap map);

    Integer addThumbNum(String id);

    Integer removeThumbNum(String id);

    Integer addPost(HashMap map);

    Integer deletePost(String id);

    ArrayList<HashMap> searchPostByTag(String tagId);
}




