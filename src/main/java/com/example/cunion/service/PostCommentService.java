package com.example.cunion.service;

import com.example.cunion.entity.PostComment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* @author Aero
* @description 针对表【post_comment(评论表)】的数据库操作Service
* @createDate 2023-10-01 18:08:20
*/
public interface PostCommentService extends IService<PostComment> {

    List<HashMap> searchAllComments(HashMap hashMap);

    Integer addComment(HashMap hashMap);

    String selectParentCommentById(String parentId);
    Integer addParentComment(HashMap map);

    Integer deleteMyComment(HashMap hashMap);

    public HashMap searchTopCommentById(String commentId);
}
