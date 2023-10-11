package com.example.cunion.service.impl;

import com.example.cunion.entity.Message;
import com.example.cunion.exception.CunionException;
import com.example.cunion.mapper.MessageMapper;
import com.example.cunion.service.MessageService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Aero
 * @description 针对表【message(消息表)】的数据库操作Service实现
 * @createDate 2023-10-03 23:49:16
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public String insertMessage(Message message) {
        String id = message.getId();
        Set keys = redisTemplate.keys("message:myMessage:*");
        if (keys != null){
            redisTemplate.delete(keys);
        }
        Integer result = messageMapper.insertMessage(message);
        try {
            Thread.sleep(120);
        } catch (InterruptedException e) {
            throw new CunionException("删除失败！");
        }
        if (keys != null){
            redisTemplate.delete(keys);
        }
        if (result != 1){
            throw new CunionException("消息添加失败");
        }
        return id;
    }

    @Override
    public List<HashMap> searchMyMessage(HashMap map) {
        String id = map.get("receiverId").toString();
        long start = Long.parseLong(map.get("start").toString());
        long length = Long.parseLong(map.get("length").toString());
        long end = start + length - 1;
        Long size = redisTemplate.opsForList().size("message:myMessage:" + id);
        if (size > 0){
            List range = redisTemplate.opsForList().range("message:myMessage:" + id, start, end);
            return range;
        }
        ArrayList<HashMap> list = messageMapper.searchMyMessage(map);
        ArrayList<HashMap> maps = messageMapper.syncMyMessage(map);
        for (HashMap hashMap : maps){
            redisTemplate.opsForList().rightPush("message:myMessage:" + id, hashMap);
        }
        redisTemplate.expire("message:myMessage:" + id, 1, TimeUnit.HOURS);
        return list;

    }

    @Override
    public Integer deleteMessage(String messageId, String id) {
        redisTemplate.delete("message:myMessage:" + id);
        Integer result = messageMapper.deleteMessage(messageId);
        try {
            Thread.sleep(120);
        } catch (InterruptedException e) {
            throw new CunionException("删除失败！");
        }
        redisTemplate.delete("message:myMessage:" + id);
        if (result != 1){
            throw new CunionException("消息删除失败！");
        }
        return result;
    }
}




