package com.example.cunion.controller.form;

import lombok.Data;

@Data
public class LoginForm {
    /**
     * 接收前端传来的账号
     * 长度不超过16位
     * 非空
     */
    private String account;
    /**
     * 接收前端传来的密码
     * 长度8-16
     * 非空
     */
    private String password;


}
