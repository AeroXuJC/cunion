package com.example.cunion.mapper;

import com.example.cunion.entity.RotationChart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Aero
* @description 针对表【rotation_chart(轮播图表)】的数据库操作Mapper
* @createDate 2023-10-03 14:32:22
* @Entity com.example.cunion.entity.RotationChart
*/
public interface RotationChartMapper extends BaseMapper<RotationChart> {

    ArrayList<HashMap> searchAllRotationChart();

}




