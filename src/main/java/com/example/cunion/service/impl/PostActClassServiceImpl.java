package com.example.cunion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cunion.entity.PostActClass;
import com.example.cunion.service.PostActClassService;
import com.example.cunion.mapper.PostActClassMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Aero
* @description 针对表【post_act_class(帖子活动分类表)】的数据库操作Service实现
* @createDate 2023-09-29 22:10:20
*/
@Service
public class PostActClassServiceImpl extends ServiceImpl<PostActClassMapper, PostActClass>
    implements PostActClassService{

    @Resource
    private PostActClassMapper postActClassMapper;

    @Override
    public ArrayList<HashMap> searchAllPostClass() {
        ArrayList<HashMap> list = postActClassMapper.searchAllPostClass();
        return list;
    }
}




