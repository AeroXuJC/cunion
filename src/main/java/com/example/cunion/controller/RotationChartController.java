package com.example.cunion.controller;

import com.example.cunion.common.R;
import com.example.cunion.service.RotationChartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/rotation")
public class RotationChartController {

    @Resource
    private RotationChartService rotationChartService;

    @GetMapping("/searchAllRotationChart")
    public R searchAllRotationChart(@RequestHeader("token") String token){
        ArrayList<HashMap> list = rotationChartService.searchAllRotationChart();
        return R.ok().put("result", list);
    }
}
