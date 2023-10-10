package com.example.cunion.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 菜品表
 * @TableName dish
 */
@TableName(value ="dish")
@Data
public class Dish implements Serializable {
    /**
     * 菜品id
     */
    @TableId
    private String id;

    /**
     * 菜品名字
     */
    private String dish_name;

    /**
     * 菜品描述
     */
    private String dish_description;

    /**
     * 菜品价格
     */
    private Double dish_price;

    /**
     * 菜品图片
     */
    private String picture;

    /**
     * 菜品口味
     */
    private String dish_flavor;

    /**
     * 所属商家id
     */
    private String shop_id;

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