package com.example.cunion.controller;

import com.example.cunion.common.R;
import com.qiniu.util.Auth;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upLoad")
public class UpLoadController {

    @GetMapping("/upLoad")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R upLoad(@RequestHeader("token") String token){
        String accessKey = "0-g7dHkM8qj24G2HrcPTo0EPpjcR_v1dj0q8bucB";
        String secretKey = "L-BAqAxYUvrUPjhOkgwWb__RHSmKN7J0R8IpBGeV";
        String bucket = "cunion";

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        System.out.println(upToken);
        return R.ok().put("result", upToken);
    }
}
