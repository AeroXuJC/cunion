package com.example.cunion.controller;

import com.example.cunion.common.R;
import com.example.cunion.controller.form.ActClassForm;
import com.example.cunion.service.ActClassService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/actClass")
public class ActClassController {

    @Resource
    private ActClassService actClassService;

    @PostMapping("/searchAllActClass")
    public R searchAllActClass(@RequestHeader String token, @RequestBody ActClassForm actClassForm){
        String position = actClassForm.getPosition();
        ArrayList<HashMap> map = actClassService.searchAllActClass(position);
        return R.ok("查询所有成功！").put("result", map);
    }

}
