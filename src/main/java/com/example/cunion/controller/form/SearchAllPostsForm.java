package com.example.cunion.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SearchAllPostsForm {
    @NotBlank
    private int start;
    @NotBlank
    private int length;

    private String searchValue;
}
