package com.example.cunion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 评论表
 * @TableName post_comment
 */
@TableName(value ="post_comment")
@Data
public class PostComment implements Serializable {
    /**
     * 评论id
     */
    @TableId
    private String id;

    /**
     * 评论者id
     */
    private String userId;

    /**
     * 评论的帖子id
     */
    private String postId;

    /**
     * 父评论id
     */
    private String parentId;

    /**
     * 子评论id
     */
    private String rootId;

    /**
     * 图片
     */
    private String picture;

    /**
     * 评论内容
     */
    private String commentContent;

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