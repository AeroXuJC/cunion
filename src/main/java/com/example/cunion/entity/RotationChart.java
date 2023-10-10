package com.example.cunion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 轮播图表
 * @TableName rotation_chart
 */
@TableName(value ="rotation_chart")
@Data
public class RotationChart implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private String id;

    /**
     * 轮播图标题
     */
    private String title;

    /**
     * 轮播图图片
     */
    private String image;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 0-未删除 1-已删除
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}