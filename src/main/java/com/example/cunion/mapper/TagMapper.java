package com.example.cunion.mapper;

import com.example.cunion.entity.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Aero
* @description 针对表【tag(标签表)】的数据库操作Mapper
* @createDate 2023-10-01 15:14:25
* @Entity com.example.cunion.entity.Tag
*/
public interface TagMapper extends BaseMapper<Tag> {

    HashMap searchTagById(String id);

    ArrayList<HashMap> searchTagByClassId(String classId);


}




