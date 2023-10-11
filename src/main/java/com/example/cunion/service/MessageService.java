package com.example.cunion.service;

import com.example.cunion.entity.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* @author Aero
* @description 针对表【message(消息表)】的数据库操作Service
* @createDate 2023-10-03 23:49:16
*/
public interface MessageService{

    String insertMessage(Message message);

    List<HashMap> searchMyMessage(HashMap map);

    Integer deleteMessage(String messageId, String id);

}
