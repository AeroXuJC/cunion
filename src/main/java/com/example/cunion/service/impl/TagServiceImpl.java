package com.example.cunion.service.impl;

import com.example.cunion.mapper.DishMapper;
import com.example.cunion.mapper.PostActClassMapper;
import com.example.cunion.mapper.TagMapper;
import com.example.cunion.service.DishService;
import com.example.cunion.service.TagService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class TagServiceImpl implements TagService {

    @Resource
    private TagMapper tagMapper;
    @Resource
    private PostActClassMapper postActClassMapper;

    @Override
    public ArrayList<HashMap> searchTagByClassId() {
        ArrayList<HashMap> list = postActClassMapper.searchAllPostClass();
        if (list != null){
            for (int i = 0; i < list.size(); i++) {
                String id = list.get(i).get("id").toString();
                ArrayList<HashMap> arrayList = tagMapper.searchTagByClassId(id);
                list.get(i).put("tag", arrayList);
            }
        }
        return list;
    }
}
