package com.example.cunion.controller.form;

import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
public class UpdateUserInfoForm {

    private String id;

    private String nickname;

    private String name;

    private String phone;

    private String email;

    private String address;

    private String gender;

    private String stuNum;

    private String avatar;
}
