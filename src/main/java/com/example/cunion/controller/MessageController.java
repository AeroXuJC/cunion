package com.example.cunion.controller;

import cn.hutool.json.JSONObject;
import com.example.cunion.common.R;
import com.example.cunion.config.shiro.JwtUtil;
import com.example.cunion.controller.form.SearchMyMessageForm;
import com.example.cunion.controller.form.SendMessageForm;
import com.example.cunion.entity.Message;
import com.example.cunion.entity.User;
import com.example.cunion.service.MessageService;
import com.example.cunion.service.UserService;
import com.example.cunion.util.MessageTask;
import com.example.cunion.util.StringSnowflakeIdGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {
    @Resource
    private MessageService messageService;
    @Resource
    private MessageTask messageTask;
    @Resource
    private UserService userService;
    @Resource
    private JwtUtil jwtUtil;
   @PostMapping("/sendMessage")
   @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R sendMessage(@RequestHeader("token") String token, @RequestBody SendMessageForm sendMessageForm){
        // 创建一个StringSnowflakeIdGenerator对象，参数分别为1和1
        StringSnowflakeIdGenerator idGenerator = new StringSnowflakeIdGenerator(1, 1);
        // 创建一个Message对象
        Message message = new Message();
        // 使用idGenerator生成一个消息id
        message.setId(idGenerator.nextId());
        // 设置消息接收者id
        message.setReceiverId(sendMessageForm.getReceiverId());
        // 创建一个HashMap对象
        HashMap map = new HashMap();
        // 将发送消息表单中的内容添加到HashMap中
        map.put("content", sendMessageForm.getContent());
        map.put("sendTime", sendMessageForm.getSendTime());
        map.put("nickname", sendMessageForm.getNickname());
        map.put("senderId", sendMessageForm.getSenderId());
        map.put("avatar", sendMessageForm.getAvatar());
        map.put("locationId", sendMessageForm.getLocationId());
        map.put("locationName", sendMessageForm.getLocationName());
        map.put("className", sendMessageForm.getClassName());
        map.put("postId", sendMessageForm.getPostId());
        // 使用ObjectMapper将HashMap转换为JSON字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // 将JSON字符串设置为消息内容
        message.setContent(jsonString);
        // 异步发送消息
        messageTask.sendAsync(sendMessageForm.getReceiverId(), message);
        // 返回发送成功信息
        return R.ok("消息发送成功！");
    }

    @GetMapping("/getMessage")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R getMessage(@RequestHeader("token") String token, @RequestParam("receiverId") String receiverId){
        // 接收消息
        List<HashMap> list = messageTask.receiveAsync(receiverId);
        // 返回接收消息成功信息
        return R.ok("消息接收成功！").put("result", list);
    }

    @PostMapping("/searchMyMessage")
    public R searchMyMessage(@RequestHeader("token") String token, @RequestBody SearchMyMessageForm searchMyMessageForm){
        // 获取当前用户id
        String receiverId = jwtUtil.getUserId(token);
        // 创建一个HashMap对象
        HashMap map = new HashMap();
        // 获取搜索消息表单中的起始位置和搜索长度
        Integer start = searchMyMessageForm.getStart();
        Integer length = searchMyMessageForm.getLength();
        // 计算起始位置
        start = (start - 1) * length;
        // 将搜索消息表单中的参数添加到HashMap中
        map.put("start", start);
        map.put("length", length);
        map.put("receiverId", receiverId);
        // 调用消息服务中的搜索消息方法
        List<HashMap> list = messageService.searchMyMessage(map);
        // 创建一个ArrayList对象
        ArrayList arrayList = new ArrayList();
        // 遍历list，将消息内容转换为JSON字符串，并将消息id添加到JSON字符串中
        for (int i = 0; i < list.size(); i++) {
            Object content = list.get(i).get("content");
            Object id = list.get(i).get("id");
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JSONObject jsonObject = objectMapper.readValue(content.toString(), JSONObject.class);
                jsonObject.putOnce("id", id);
                arrayList.add(jsonObject);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        // 返回搜索消息成功信息
        return R.ok().put("result", arrayList);
    }

    @GetMapping("/deleteMessage")
    public R deleteMessage(@RequestHeader("token") String token, @RequestParam("messageId") String messageId){
        // 获取当前用户id
        String userId = jwtUtil.getUserId(token);
        // 调用消息服务中的删除消息方法
        Integer result = messageService.deleteMessage(messageId, userId);
        // 返回删除消息成功信息
        return R.ok();
    }
}
