package com.example.cunion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 帖子表
 * @TableName post
 */
@TableName(value ="post")
@Data
public class Post implements Serializable {
    /**
     * 帖子id
     */
    @TableId
    private String id;

    /**
     * 用户id
     */
    private String user_id;

    /**
     * 帖子内容
     */
    private String post_content;

    /**
     * 帖子地址
     */
    private String post_address;

    /**
     * 访客数量
     */
    private Integer visitor_num;

    /**
     * 图片
     */
    private String picture;

    /**
     * 点赞列表
     */
    private String thumbList;

    /**
     * 标签id列表
     */
    private String tagList;

    /**
     * 点赞数量
     */
    private Integer thumb_num;

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