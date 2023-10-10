package com.example.cunion.controller;

import com.example.cunion.common.R;
import com.example.cunion.service.TimetableService;
import com.example.cunion.util.StringSnowflakeIdGenerator;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Resource
    private TimetableService timetableService;

    @PostMapping("/xls")
    public R uploadXLS(@RequestParam("file") MultipartFile file) {
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
                List<String> list = new ArrayList();
                // 遍历行和列，读取单元格内容
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        // 根据需要处理单元格内容，例如输出到控制台
                        list.add(cell.toString());
                    }
                }
                for (int i = 0; i < list.size(); i++) {
                    if ("".equals(list.get(i).trim())) {
                        list.set(i, "空空空空");
                    }
                }
//                StringSnowflakeIdGenerator stringSnowflakeIdGenerator = new StringSnowflakeIdGenerator(1, 1);
//                HashMap map = new HashMap();
//                map.put("content", list.toString());
//                map.put("id", stringSnowflakeIdGenerator.nextId());
                System.out.println(list.toString());
                // 关闭工作簿和输入流
                workbook.close();
                inputStream.close();
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
