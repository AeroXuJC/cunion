package com.example.cunion.service.impl;

import com.example.cunion.mapper.ActClassMapper;
import com.example.cunion.service.ActClassService;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@Configuration
public class ActClassServiceImpl implements ActClassService {

    @Resource
    private ActClassMapper actClassMapper;

    @Override
    public ArrayList searchAllActClass(String position) {
        ArrayList<HashMap> list = actClassMapper.searchAllActClass(position);
        return list;
    }
}
