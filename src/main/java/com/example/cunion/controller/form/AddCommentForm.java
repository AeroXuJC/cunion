package com.example.cunion.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;

@Data
public class AddCommentForm {

    @NotBlank
    private String shopId;

    @NotBlank
    private String commentContent;

    private String parentId;

    private String picture;
}
