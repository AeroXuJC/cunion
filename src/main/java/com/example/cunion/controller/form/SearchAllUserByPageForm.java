package com.example.cunion.controller.form;

import lombok.Data;

@Data
public class SearchAllUserByPageForm {

    private Integer start;

    private Integer length;

    private String searchValue;
}
