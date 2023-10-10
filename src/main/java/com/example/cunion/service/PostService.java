package com.example.cunion.service;

import com.example.cunion.entity.Post;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* @author Aero
* @description 针对表【post(帖子表)】的数据库操作Service
* @createDate 2023-09-29 23:00:25
*/
public interface PostService extends IService<Post> {


    List<HashMap> searchAllPosts(HashMap map);

    HashMap searchAllPostById(HashMap map);

    Integer addPost(HashMap map);

    Integer deletePost(String id);

    List<HashMap> searchPostByTag(String classId);

}
