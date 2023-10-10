package com.example.cunion.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;

@Data
public class RegisterForm {
    /**
     * 接收前端传来的账号
     * 长度不超过16位
     * 非空
     */
    @NotBlank(message = "账号不能为空！")
    private String account;
    /**
     * 接收前端传来的密码
     * 长度8-16
     * 非空
     */
    @NotBlank(message = "密码不能为空！")
    private String password;
    /**
     * 接收前端传来的确认密码
     * 长度8-16
     * 非空
     */
    @NotBlank(message = "不能为空！")
    private String CheckPassword;


}
