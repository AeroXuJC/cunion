package com.example.cunion.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 活动分类表
 * @TableName act_class
 */
@TableName(value ="act_class")
@Data
public class ActClass implements Serializable {
    /**
     * 活动分类id
     */
    @TableId
    private String id;

    /**
     * 活动分类名称
     */
    private String act_class_name;

    /**
     * 图片
     */
    private String picture;

    /**
     * 创建时间
     */
    private Date create_time;

    /**
     * 更新时间
     */
    private Date update_time;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer is_deleted;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}