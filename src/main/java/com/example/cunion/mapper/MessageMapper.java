package com.example.cunion.mapper;

import com.example.cunion.entity.Message;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Aero
* @description 针对表【message(消息表)】的数据库操作Mapper
* @createDate 2023-10-03 23:49:16
* @Entity com.example.cunion.entity.Message
*/
public interface MessageMapper{


    Integer insertMessage(Message message);

    ArrayList<HashMap> searchMyMessage(HashMap map);

    ArrayList<HashMap> syncMyMessage(HashMap map);

    Integer deleteMessage(String messageId);
}




