package com.example.cunion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName operation
 */
@TableName(value ="operation")
@Data
public class Operation implements Serializable {
    /**
     * 用户操作表id
     */
    @TableId
    private String id;

    /**
     * 用户操作表图标/图片
     */
    private String picture;

    /**
     * 用户操作表标题
     */
    private String title;

    /**
     * 创建时间
     */
    private Date create_time;

    /**
     * 更新时间
     */
    private Date update_time;

    /**
     * 0-未删除 1-已删除
     */
    @TableLogic
    private Integer is_deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}