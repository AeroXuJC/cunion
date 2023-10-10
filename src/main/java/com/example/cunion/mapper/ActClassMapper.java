package com.example.cunion.mapper;

import com.example.cunion.entity.ActClass;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.shiro.crypto.hash.Hash;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author 37026
* @description 针对表【act_class(活动分类表)】的数据库操作Mapper
* @createDate 2023-09-13 19:48:35
* @Entity com.example.cunion.entity.ActClass
*/
public interface ActClassMapper extends BaseMapper<ActClass> {

    ArrayList<HashMap> searchAllActClass(String position);
}




