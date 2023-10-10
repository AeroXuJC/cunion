package com.example.cunion.controller;

import com.example.cunion.common.R;
import com.example.cunion.service.OperationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/operation")
public class OperationController {

    @Resource
    private OperationService operationService;

    @GetMapping("/getAllOperation")
    public R getAllOperation(@RequestHeader("token") String token){
        ArrayList<HashMap> list = operationService.searchAllOperation();
        return R.ok().put("result", list);
    }
}
