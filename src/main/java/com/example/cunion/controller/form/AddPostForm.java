package com.example.cunion.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AddPostForm {

    @NotBlank
    private String postContent;

    @NotBlank
    private String postAddress;

    @NotBlank
    private String tagList;

    private String picture;

}
