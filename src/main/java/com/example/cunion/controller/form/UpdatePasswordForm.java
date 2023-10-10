package com.example.cunion.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdatePasswordForm {
    @NotBlank
    private String password;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String checkPassword;
}
