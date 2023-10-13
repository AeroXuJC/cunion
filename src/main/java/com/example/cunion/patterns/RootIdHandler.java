package com.example.cunion.patterns;

import cn.hutool.json.JSONUtil;
import com.example.cunion.mapper.CommentMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class RootIdHandler implements DataHandler {

    @Resource
    private CommentMapper commentMapper;

    @Override
    public ArrayList<HashMap> handle(HashMap map) {
        // 处理 rootId 的逻辑
        Object rootId = map.get("rootId");
        ArrayList<HashMap> arrayList = new ArrayList<>();
        if (rootId != null && !"[]".equals(rootId.toString()) && !"".equals(rootId.toString())) {
            List<String> list = JSONUtil.toList(rootId.toString(), String.class);
            for (int i = list.size() - 1; i >= 0; i--) {
                HashMap hashMap = commentMapper.searchCommentById(list.get(i));
                arrayList.add(hashMap);
            }
        }
        return arrayList;
    }
}