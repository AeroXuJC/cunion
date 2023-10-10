package com.example.cunion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cunion.entity.Operation;
import com.example.cunion.mapper.OperationMapper;
import com.example.cunion.service.OperationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Aero
* @description 针对表【operation】的数据库操作Service实现
* @createDate 2023-09-29 17:41:08
*/
@Service
public class OperationServiceImpl extends ServiceImpl<OperationMapper, Operation>
    implements OperationService {

    @Resource
    private OperationMapper operationMapper;
    @Override
    public ArrayList<HashMap> searchAllOperation() {
        ArrayList<HashMap> list = operationMapper.searchAllOperation();
        return list;
    }
}




