package com.example.cunion.mapper;

import com.example.cunion.entity.PostActClass;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Aero
* @description 针对表【post_act_class(帖子活动分类表)】的数据库操作Mapper
* @createDate 2023-09-29 22:10:20
* @Entity com.example.cunion.entity.PostActClass
*/
public interface PostActClassMapper extends BaseMapper<PostActClass> {

    ArrayList<HashMap> searchAllPostClass();

}




