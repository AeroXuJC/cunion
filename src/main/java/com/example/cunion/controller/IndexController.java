package com.example.cunion.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "首页模块")
@RestController
public class IndexController {

    @ApiImplicitParam(name = "name", value = "姓名", required = true)
    @ApiOperation(value = "向客人问好")
    @GetMapping("/sayHi")
    @RequiresPermissions(value = {"ROOT"})
    public ResponseEntity<String> sayHi(@RequestParam(value = "name") String name, @RequestHeader("token") String token) {
        return ResponseEntity.ok("Hi:" + name);
    }
}