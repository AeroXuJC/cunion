package com.example.cunion.service;


import java.util.HashMap;

/**
* @author Aero
* @description 针对表【timetable(课程表)】的数据库操作Service
* @createDate 2023-09-26 18:01:13
*/
public interface TimetableService{

    Integer addTimetable(HashMap map);

    HashMap searchContentByUserId(String userId);

    Integer updateContent(HashMap map);
}
