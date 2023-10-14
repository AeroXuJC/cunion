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
        //获取消息id
        String id = message.getId();
        //获取所有消息的key
        Set keys = redisTemplate.keys("message:myMessage:*");
        //如果key不为空，则删除
        if (keys != null){
            redisTemplate.delete(keys);
        }
        //插入消息
        Integer result = messageMapper.insertMessage(message);
        try {
            //休眠120s
            Thread.sleep(120);
        } catch (InterruptedException e) {
            throw new CunionException("删除失败！");
        }
        //如果key不为空，则删除
        if (keys != null){
            redisTemplate.delete(keys);
        }
        //判断是否插入成功
        if (result != 1){
            throw new CunionException("消息添加失败");
        }
        return id;
    }

    @Override
    public List<HashMap> searchMyMessage(HashMap map) {
        //获取接收者id
        String id = map.get("receiverId").toString();
        //获取开始位置
        long start = Long.parseLong(map.get("start").toString());
        //获取长度
        long length = Long.parseLong(map.get("length").toString());
        //获取结束位置
        long end = start + length - 1;
        //获取消息列表长度
        Long size = redisTemplate.opsForList().size("message:myMessage:" + id);
        //如果消息列表不为空
        if (size > 0){
            //获取消息列表
            List range = redisTemplate.opsForList().range("message:myMessage:" + id, start, end);
            return range;
        }
        //从数据库中获取消息
        ArrayList<HashMap> list = messageMapper.searchMyMessage(map);
        //从数据库中同步消息
        ArrayList<HashMap> maps = messageMapper.syncMyMessage(map);
        //将同步消息添加到消息列表
        for (HashMap hashMap : maps){
            redisTemplate.opsForList().rightPush("message:myMessage:" + id, hashMap);
        }
        //设置消息列表过期时间
        redisTemplate.expire("message:myMessage:" + id, 1, TimeUnit.HOURS);
        return list;

    }

    @Override
    public Integer deleteMessage(String messageId, String id) {
        //删除消息
        redisTemplate.delete("message:myMessage:" + id);
        //删除消息
        Integer result = messageMapper.deleteMessage(messageId);
        try {
            //休眠120s
            Thread.sleep(120);
        } catch (InterruptedException e) {
            throw new CunionException("删除失败！");
        }
        //删除消息
        redisTemplate.delete("message:myMessage:" + id);
        //判断是否删除成功
        if (result != 1){
            throw new CunionException("消息删除失败！");
        }
        return result;
    }
}




