package com.example.cunion.service;

import java.util.ArrayList;
import java.util.HashMap;

public interface ActClassService {

    /**
     * 查找所有活动分类
     * @return
     */
    ArrayList<HashMap> searchAllActClass(String position);
}
