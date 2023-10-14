package com.example.cunion.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cunion.entity.Timetable;

import java.util.HashMap;

/**
* @author Aero
* @description 针对表【timetable(课程表)】的数据库操作Mapper
* @createDate 2023-09-26 18:01:13
* @Entity generator.domain.Timetable
*/
public interface TimetableMapper {

    Integer addTimetable(HashMap map);

    HashMap searchContentByUserId(String userId);

    Integer updateContent(HashMap map);

    Integer deleteTimetable(String id);
}




