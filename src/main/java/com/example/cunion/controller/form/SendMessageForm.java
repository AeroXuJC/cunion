package com.example.cunion.controller.form;

import com.alibaba.fastjson.JSON;
import lombok.Data;

@Data
public class SendMessageForm {

    private String receiverId;

    private String content;

    private String sendTime;

    private String nickname;

    private String senderId;

    private String avatar;

    private String locationId;

    private String locationName;

    private String className;

    private String postId;

}
