package com.example.cunion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 商家表
 * @TableName shop
 */
@TableName(value ="shop")
@Data
public class Shop implements Serializable {
    /**
     * 商家id
     */
    @TableId
    private String id;

    /**
     * 商家名称
     */
    private String shop_name;

    /**
     * 商家图片
     */
    private String picture;

    /**
     * 商家地址
     */
    private String shop_address;

    /**
     * 商家描述
     */
    private String shop_description;

    /**
     * 商家所在楼层
     */
    private Integer floor;

    /**
     * 商家评分
     */
    private String shop_score;

    /**
     * 点赞列表
     */
    private Object thumb;

    /**
     * 收藏列表
     */
    private Object collect;

    /**
     * 评论列表
     */
    private ArrayList<Comment> comment = new ArrayList<>();

    /**
     * 创建时间
     */
    private Date create_time;

    /**
     * 更新时间
     */
    private Date update_time;


    /**
     * 逻辑删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer is_deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}