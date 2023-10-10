package com.example.cunion.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserForm {
    @NotBlank
    private String id;
}
