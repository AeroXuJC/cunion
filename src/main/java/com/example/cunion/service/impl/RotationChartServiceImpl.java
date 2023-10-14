package com.example.cunion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cunion.entity.RotationChart;
import com.example.cunion.service.RotationChartService;
import com.example.cunion.mapper.RotationChartMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Aero
* @description 针对表【rotation_chart(轮播图表)】的数据库操作Service实现
* @createDate 2023-10-03 14:32:22
*/
@Service
public class RotationChartServiceImpl extends ServiceImpl<RotationChartMapper, RotationChart>
    implements RotationChartService{
    @Resource
    private RotationChartMapper rotationChartMapper;

    @Override
    public ArrayList<HashMap> searchAllRotationChart() {
        //调用rotationChartMapper的searchAllRotationChart()方法，获取所有的旋转图表
        ArrayList<HashMap> list = rotationChartMapper.searchAllRotationChart();
        //返回获取的旋转图表
        return list;
    }
}




