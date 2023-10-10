package com.example.cunion.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.example.cunion.entity.Message;
import com.example.cunion.exception.CunionException;
import com.example.cunion.service.MessageService;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.*;

@Component
@Slf4j
public class MessageTask {
    @Autowired
    private ConnectionFactory factory;

    @Autowired
    private MessageService messageService;


    public void send(String topic, Message entity) {
        String id = messageService.insertMessage(entity);
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel();
        ) {
            channel.queueDeclare(topic, true, false, false, null);
            HashMap map = new HashMap();
            map.put("messageId", id);
            AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().headers(map).build();
            channel.basicPublish("", topic, properties, entity.getContent().getBytes());
            log.debug("消息发送成功");
        } catch (Exception e) {
            log.error("执行异常", e);
            throw new CunionException("向MQ发送消息失败");
        }
    }

    @Async
    public void sendAsync(String topic, Message entity) {
        send(topic, entity);
    }

    public List<HashMap> receive(String topic) {
        int i = 0;
        String message = "";
        ArrayList<HashMap> list = new ArrayList<>();
        HashMap hashMap = new HashMap();
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel();
        ) {
            channel.queueDeclare(topic, true, false, false, null);
            while (true) {
                GetResponse response = channel.basicGet(topic, false);
                if (response != null) {
                    AMQP.BasicProperties properties = response.getProps();
                    Map<String, Object> map = properties.getHeaders();
                    String messageId = map.get("messageId").toString();
                    byte[] body = response.getBody();
                    message = new String(body);
                    log.debug("从RabbitMQ接收的消息：" + message);
                    System.out.println("从RabbitMQ接收的消息：" + message);
                    hashMap.put("message", message);
                    list.add(hashMap);
                    long deliveryTag = response.getEnvelope().getDeliveryTag();
                    channel.basicAck(deliveryTag, false);
                    i++;
                }
                else {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("执行异常", e);
            throw new CunionException("接收消息失败");
        }
        return list;
    }

    @Async
    public List<HashMap> receiveAsync(String topic) {
        return receive(topic);
    }

    public void deleteQueue(String topic){
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel();
        ) {
            channel.queueDelete(topic);
            log.debug("消息队列成功删除");
        }catch (Exception e) {
            log.error("删除队列失败", e);
            throw new CunionException("删除队列失败");
        }
    }

    @Async
    public void deleteQueueAsync(String topic){
        deleteQueue(topic);
    }

}
