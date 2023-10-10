package com.example.cunion.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CommentForm {
    @NotBlank
    private int start;

    @NotBlank
    private int length;

    @NotBlank
    private String shopId;
}
