package com.example.cunion.config;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Canal + RabbitMQ 监听数据库数据变化
 *
 */
@Slf4j
@Component
public class CanalListener {

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "canal.queue", durable = "true"),
                    exchange = @Exchange(value = "canal.exchange"),
                    key = "canal.routing.key"
            )
    })
    public void handleDataChange(String message) {
        JSONObject object = JSONObject.parseObject(message);
        log.info("Canal监听到数据发生变化\n库名：{}\n表名：{}\n类型：{}\n数据：{}", object.getString("database"), object.getString("table"), object.getString("type"), object.getString("data"));
        /**
         * TODO 同步Redis
         */
    }
}