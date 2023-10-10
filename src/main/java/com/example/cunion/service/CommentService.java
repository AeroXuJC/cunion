package com.example.cunion.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface CommentService {
    List<HashMap> searchAllComments(HashMap hashMap);

    Integer addComment(HashMap hashMap);

    String selectParentCommentById(String parentId);
    Integer addParentComment(HashMap map);

    Integer deleteMyComment(HashMap hashMap);

    HashMap searchTopCommentById(String commentId);

}
