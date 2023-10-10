package com.example.cunion.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ShopForm {
    @NotBlank
    private int start;

    @NotBlank
    private int length;

    @NotBlank
    private String position;

    private Integer floor;

    private String searchValue;
}
