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
    public R sendMessage(@RequestHeader("token") String token, @RequestBody SendMessageForm sendMessageForm){
        StringSnowflakeIdGenerator idGenerator = new StringSnowflakeIdGenerator(1, 1);
        Message message = new Message();
        message.setId(idGenerator.nextId());
        message.setReceiverId(sendMessageForm.getReceiverId());
        HashMap map = new HashMap();
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
        message.setContent(jsonString);
        messageTask.sendAsync(sendMessageForm.getReceiverId(), message);
        return R.ok("消息发送成功！");
    }

    @GetMapping("/getMessage")
    public R getMessage(@RequestHeader("token") String token, @RequestParam("receiverId") String receiverId){
        List<HashMap> list = messageTask.receiveAsync(receiverId);
        return R.ok("消息接收成功！").put("result", list);
    }

    @PostMapping("/searchMyMessage")
    public R searchMyMessage(@RequestHeader("token") String token, @RequestBody SearchMyMessageForm searchMyMessageForm){
        String receiverId = jwtUtil.getUserId(token);
        HashMap map = new HashMap();
        Integer start = searchMyMessageForm.getStart();
        Integer length = searchMyMessageForm.getLength();
        start = (start - 1) * length;
        map.put("start", start);
        map.put("length", length);
        map.put("receiverId", receiverId);
        ArrayList<HashMap> list = messageService.searchMyMessage(map);
        ArrayList arrayList = new ArrayList();
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
        return R.ok().put("result", arrayList);
    }

    @GetMapping("/deleteMessage")
    public R deleteMessage(@RequestHeader("token") String token, @RequestParam("messageId") String messageId){
        Integer result = messageService.deleteMessage(messageId);
        return R.ok();
    }
}
