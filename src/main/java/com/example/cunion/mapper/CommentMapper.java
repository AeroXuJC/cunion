package com.example.cunion.mapper;

import com.example.cunion.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.shiro.crypto.hash.Hash;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author 37026
* @description 针对表【comment(评论表)】的数据库操作Mapper
* @createDate 2023-09-15 19:25:57
* @Entity com.example.cunion.entity.Comment
*/
public interface CommentMapper extends BaseMapper<Comment> {
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




