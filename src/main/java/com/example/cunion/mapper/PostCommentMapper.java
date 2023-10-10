package com.example.cunion.mapper;

import com.example.cunion.entity.PostComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Aero
* @description 针对表【post_comment(评论表)】的数据库操作Mapper
* @createDate 2023-10-01 18:08:20
* @Entity com.example.cunion.entity.PostComment
*/
public interface PostCommentMapper extends BaseMapper<PostComment> {

    ArrayList<HashMap> searchAllComments(HashMap hashMap);

    HashMap searchCommentById(String id);

    Integer addComment(HashMap hashMap);

    String selectParentCommentById(String parentId);

    Integer addParentComment(HashMap map);

    Integer deleteMyComment(HashMap hashMap);

    HashMap searchRootCommentById(String commentId);

    Integer deleteCommentById(String commentId);

    Integer updateParentRootId(HashMap map);

}




