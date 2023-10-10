package com.example.cunion.controller;

import com.example.cunion.common.R;
import com.example.cunion.service.TagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/tag")
public class TagController {
    @Resource
    private TagService tagService;

    @GetMapping("/searchTag")
    public R searchTag(@RequestHeader("token") String token){
        ArrayList<HashMap> list = tagService.searchTagByClassId();
        return R.ok().put("result", list);
    }
}
