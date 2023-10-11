package com.example.cunion.controller.form;

import lombok.Data;

@Data
public class SearchMyPostForm {

    private Integer start;

    private Integer length;

    private String searchValue;
}
