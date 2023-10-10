package com.example.cunion;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.example.cunion.entity.Message;
import com.example.cunion.entity.Post;
import com.example.cunion.mapper.CommentMapper;
import com.example.cunion.mapper.MessageMapper;
import com.example.cunion.mapper.UserMapper;
import com.example.cunion.util.MessageTask;
import com.example.cunion.util.StringSnowflakeIdGenerator;
import com.qiniu.util.Auth;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.*;

@SpringBootTest
class CunionApplicationTests {

    @Resource
    private UserMapper userMapper;

//    @Resource
//    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MessageTask messageTask;

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        Set<String> keys = redisTemplate.keys( "shop:searchAllShops:*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }


    @Resource
    private CommentMapper commentMapper;

    @Test
    public ArrayList<HashMap> searchAllComments(HashMap hashMap) {
        ArrayList<HashMap> maps = commentMapper.searchAllComments(hashMap);
        for (HashMap map : maps) {
            System.out.println(map);
        }
        return maps;
    }
}
