package com.example.cunion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.cunion.entity.Operation;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Aero
* @description 针对表【operation】的数据库操作Service
* @createDate 2023-09-29 17:41:08
*/
public interface OperationService extends IService<Operation> {

    ArrayList<HashMap> searchAllOperation();

}
