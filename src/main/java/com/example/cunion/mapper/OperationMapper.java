package com.example.cunion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cunion.entity.Operation;

import java.util.ArrayList;
import java.util.HashMap;


/**
* @author Aero
* @description 针对表【operation】的数据库操作Mapper
* @createDate 2023-09-29 17:41:08
* @Entity generator.domain.Operation
*/
public interface OperationMapper extends BaseMapper<Operation> {

    ArrayList<HashMap> searchAllOperation();

}




