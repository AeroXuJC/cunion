package com.example.cunion.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DeleteMyPostCommentForm {

    @NotBlank
    private String commentId;

    @NotBlank
    private String postId;
}
