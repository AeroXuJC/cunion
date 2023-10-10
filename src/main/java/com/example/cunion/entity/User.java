package com.example.cunion.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 用户id
     */
    @TableId
    private String id;

    /**
     *  用户昵称
     */
    private String nickname;

    /**
     *  用户账号
     */
    private String user_account;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户密码
     */
    private String password;

    /**
     *   用户电话
     */
    private String phone;

    /**
     *   用户电话
     */
    private String thumbList;

    /**
     *   用户电话
     */
    private String collectList;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户地址
     */
    private String address;

    /**
     * 用户学号
     */
    private String stu_num;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户性别
     */
    private String gender;

    /**
     *  创建时间
     */
    private Date create_time;

    /**
     * 更新时间
     */
    private Date update_time;

    /**
     * 0-未删除
     * 1-已删除
     */
    @TableLogic
    private Integer is_deleted;

    /**
     * 用户角色
     */
    private String userRole;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}