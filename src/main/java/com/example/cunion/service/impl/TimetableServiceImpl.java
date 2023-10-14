package com.example.cunion.service.impl;


import com.example.cunion.exception.CunionException;
import com.example.cunion.mapper.TimetableMapper;
import com.example.cunion.service.TimetableService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

/**
* @author Aero
* @description 针对表【timetable(课程表)】的数据库操作Service实现
* @createDate 2023-09-26 18:01:13
*/
@Service
public class TimetableServiceImpl implements TimetableService {
    @Resource
    private TimetableMapper timetableMapper;
    @Override
    public Integer addTimetable(HashMap map) {
        // 调用timetableMapper的addTimetable方法，添加课程表
        Integer result = timetableMapper.addTimetable(map);
        return result;
    }

    @Override
    public HashMap searchContentByUserId(String userId) {
        // 调用timetableMapper的searchContentByUserId方法，根据用户id搜索课程表
        HashMap map = timetableMapper.searchContentByUserId(userId);
        return map;
    }

    @Override
    public Integer updateContent(HashMap map) {
        // 调用timetableMapper的updateContent方法，更新课程表
        Integer result = timetableMapper.updateContent(map);
        if (result != 1){
            throw new CunionException("导入课表失败，请重试！");
        }
        return result;
    }

    @Override
    public Integer deleteTimetable(String id) {
        Integer result = timetableMapper.deleteTimetable(id);
        if (result != 1){
            throw new CunionException("课表删除失败！");
        }
        return result;
    }
}




