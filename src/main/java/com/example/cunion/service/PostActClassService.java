package com.example.cunion.service;

import com.example.cunion.entity.PostActClass;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Aero
* @description 针对表【post_act_class(帖子活动分类表)】的数据库操作Service
* @createDate 2023-09-29 22:10:20
*/
public interface PostActClassService extends IService<PostActClass> {

    ArrayList<HashMap> searchAllPostClass();

}
