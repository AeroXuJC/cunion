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
        // 调用postActClassMapper的searchAllPostClass方法，获取所有分类信息
        ArrayList<HashMap> list = postActClassMapper.searchAllPostClass();
        // 如果list不为空
        if (list != null){
            // 遍历list
            for (int i = 0; i < list.size(); i++) {
                // 获取当前分类的id
                String id = list.get(i).get("id").toString();
                // 调用tagMapper的searchTagByClassId方法，根据id获取标签信息
                ArrayList<HashMap> arrayList = tagMapper.searchTagByClassId(id);
                // 将标签信息添加到分类信息中
                list.get(i).put("tag", arrayList);
            }
        }
        // 返回分类信息
        return list;
    }
}
