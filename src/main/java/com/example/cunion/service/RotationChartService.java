package com.example.cunion.service;

import com.example.cunion.entity.RotationChart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Aero
* @description 针对表【rotation_chart(轮播图表)】的数据库操作Service
* @createDate 2023-10-03 14:32:22
*/
public interface RotationChartService extends IService<RotationChart> {

    ArrayList<HashMap> searchAllRotationChart();

}
