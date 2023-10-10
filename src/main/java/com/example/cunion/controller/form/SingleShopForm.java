package com.example.cunion.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SingleShopForm {
    @NotBlank
    private String shopId;
}
