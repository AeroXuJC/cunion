package com.example.cunion.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import lombok.Data;

/**
 * 评论表
 * @TableName comment
 */
@TableName(value ="comment")
@Data
public class Comment implements Serializable {
    /**
     * 评论id
     */
    @TableId
    private String id;

    /**
     * 评论人id
     */
    private String user_id;

    /**
     * 评论商家id
     */
    private String shop_id;

    /**
     * 父评论id
     */
    private String parent_id;

    /**
     * 子评论id
     */
    private String root_id;

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
     * 子评论列表
     */
    private ArrayList<Comment> child = new ArrayList<>();


    /**
     * 逻辑删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer is_deleted;

    /**
     * 评论内容
     */
    private String comment_content;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}