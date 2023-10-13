package com.example.cunion.patterns;

import java.util.ArrayList;
import java.util.HashMap;

public class PictureHandler implements DataHandler {
    @Override
    public ArrayList<HashMap> handle(HashMap map) {
        // 处理 picture 的逻辑
        Object picture = map.get("picture");
        ArrayList arrayList = new ArrayList();
        if (picture != null && !"".equals(picture.toString())) {
            String[] split = picture.toString().split(",");
            for (int i = 0; i < split.length; i++) {
                arrayList.add(split[i]);
            }
        }
        return arrayList;
    }
}