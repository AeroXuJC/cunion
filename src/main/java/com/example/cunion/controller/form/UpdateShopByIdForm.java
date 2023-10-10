package com.example.cunion.controller.form;

import lombok.Data;

@Data
public class UpdateShopByIdForm {

    private String id;

    private String shopName;

    private String shopDescription;

    private String shopAddress;

    private String workTime;

    private Double shopScore;

    private Integer isDeleted;
}
