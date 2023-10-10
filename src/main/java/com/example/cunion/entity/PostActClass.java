package com.example.cunion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 帖子活动分类表
 * @TableName post_act_class
 */
@TableName(value ="post_act_class")
@Data
public class PostActClass implements Serializable {
    /**
     * 分类活动表id
     */
    @TableId
    private String id;

    /**
     * 活动分类名
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
     * 0-未删除 1-已删除
     */
    private Integer is_deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}