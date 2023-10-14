package com.example.cunion.controller;

import com.example.cunion.common.R;
import com.example.cunion.config.shiro.JwtUtil;
import com.example.cunion.exception.CunionException;
import com.example.cunion.service.TimetableService;
import com.example.cunion.util.StringSnowflakeIdGenerator;
import org.apache.poi.ss.usermodel.*;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/timetable")
public class TimetableController {
    @Resource
    private TimetableService timetableService;

    @Resource
    private JwtUtil jwtUtil;

   @GetMapping("/getTimetable")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R getTimetable(@RequestHeader("token") String token) {
        // 获取用户ID
        String userId = jwtUtil.getUserId(token);
        // 调用服务层查询课程表
        HashMap map = timetableService.searchContentByUserId(userId);
        // 判断查询结果是否为空
        if (map == null) {
            throw new CunionException("请上传Excel课程表文件导入");
        }
//        List<String> list = JSONUtil.toList(map.get("content").toString(), String.class);
        // 将map中的content转换为list
        ArrayList list = new ArrayList();
        String content = map.get("content").toString();
        String substring = content.substring(1, content.length() - 1);
        String[] split = substring.split(",");
        for (int i = 0; i < split.length; i++) {
            list.add(split[i]);
        }
        // 返回查询结果
        return R.ok().put("result", list);
    }

    @PostMapping("/xls")
    @RequiresPermissions(value = {"user", "admin"}, logical = Logical.OR)
    public R uploadXLS(@RequestParam("file") MultipartFile file, @RequestHeader("token") String token) {
        // 获取用户ID
        String userId = jwtUtil.getUserId(token);

        // 处理上传的XLS文件，例如保存到服务器或进行其他处理
        if (!file.isEmpty()) {
            // 处理文件逻辑
            try {
                // 获取上传的Excel文件的输入流
                InputStream inputStream = file.getInputStream();

                // 使用Workbook对象来处理Excel文件
                Workbook workbook = WorkbookFactory.create(inputStream);

                // 假设要读取第一个工作表
                Sheet sheet = workbook.getSheetAt(0);
                // 创建一个list用于存储单元格内容
                List<String> list = new ArrayList();
                // 遍历行和列，读取单元格内容
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        // 根据需要处理单元格内容，例如输出到控制台
                        list.add(cell.toString());
                    }
                }
                // 遍历list，判断单元格内容是否为空，如果为空则替换为“空空空空”
                for (int i = 0; i < list.size(); i++) {
                    if ("".equals(list.get(i).trim())) {
                        list.set(i, "空空空空");
                    }
                }
                System.out.println(list.toString());
                // 关闭工作簿和输入流
                workbook.close();
                inputStream.close();
                // 调用服务层查询课程表
                HashMap resultMap = timetableService.searchContentByUserId(userId);
                // 判断查询结果是否为空
                if (resultMap != null) {
                    // 创建一个HashMap用于存储用户ID和单元格内容
                    HashMap hashMap = new HashMap();
                    hashMap.put("userId", userId);
                    hashMap.put("content", list.toString());
                    // 调用服务层更新课程表
                    Integer result = timetableService.updateContent(hashMap);
                    return R.ok();
                }
                // 调用服务层添加课程表
                StringSnowflakeIdGenerator stringSnowflakeIdGenerator = new StringSnowflakeIdGenerator(1, 1);
                HashMap map = new HashMap();
                map.put("content", list.toString());
                map.put("id", stringSnowflakeIdGenerator.nextId());
                map.put("userId", userId);
                Integer result = timetableService.addTimetable(map);
                // 判断添加结果
                if (result != 1) {
                    throw new CunionException("导入课程表失败，请重试！");
                }
                return R.ok().put("result", list);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return R.ok("上传成功！");
        } else {
            return R.error("上传失败！");
        }
    }
}
