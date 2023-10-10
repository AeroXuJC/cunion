package com.example.cunion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程表
 * @TableName timetable
 */
@TableName(value ="timetable")
@Data
public class Timetable implements Serializable {
    /**
     * 
     */
    @TableId
    private String id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 课程表详情内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date create_time;

    /**
     * 更新时间
     */
    private Date update_time;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    private Integer is_deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}