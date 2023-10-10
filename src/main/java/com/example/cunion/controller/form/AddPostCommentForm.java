package com.example.cunion.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AddPostCommentForm {

    @NotBlank
    private String postId;

    @NotBlank
    private String commentContent;

    private String parentId;

    private String picture;
}
