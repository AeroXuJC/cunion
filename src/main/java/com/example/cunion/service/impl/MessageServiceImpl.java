package com.example.cunion.service.impl;

import com.example.cunion.entity.Message;
import com.example.cunion.exception.CunionException;
import com.example.cunion.mapper.MessageMapper;
import com.example.cunion.service.MessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Aero
 * @description 针对表【message(消息表)】的数据库操作Service实现
 * @createDate 2023-10-03 23:49:16
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageMapper messageMapper;
    @Override
    public String insertMessage(Message message) {
        String id = message.getId();
        Integer result = messageMapper.insertMessage(message);
        if (result != 1){
            throw new CunionException("消息添加失败");
        }
        return id;
    }

    @Override
    public ArrayList<HashMap> searchMyMessage(HashMap map) {
        ArrayList<HashMap> list = messageMapper.searchMyMessage(map);
        return list;

    }

    @Override
    public Integer deleteMessage(String messageId) {
        Integer result = messageMapper.deleteMessage(messageId);
        if (result != 1){
            throw new CunionException("消息删除失败！");
        }
        return result;
    }
}




