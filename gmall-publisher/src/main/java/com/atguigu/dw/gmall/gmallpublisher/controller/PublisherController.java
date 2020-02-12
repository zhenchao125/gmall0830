package com.atguigu.dw.gmall.gmallpublisher.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.dw.gmall.gmallpublisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author lzc
 * @Date 2020/2/12 10:16
 */
@RestController
public class PublisherController {
    /* http://localhost:8070/realtime-total?date=2020-02-11

        [{"id":"dau","name":"新增日活","value":1200},
        {"id":"new_mid","name":"新增设备","value":233} ]

        想得到json数组, 可以在代码中创建java的数组(集合..), 让后统计
        json统计直接转换成json数组
     */

    @Autowired
    public PublisherService service;

    @GetMapping("/realtime-total")
    public String getDau(@RequestParam("date") String date) {
        List<Map<String, String>> result = new ArrayList<>();

        HashMap<String, String> map1 = new HashMap<>();
        map1.put("id", "dau");
        map1.put("name", "新增日活");
        map1.put("value", service.getDau(date) + "");
        result.add(map1);

        HashMap<String, String> map2 = new HashMap<>();
        map2.put("id", "new_mid");
        map2.put("name", "新增设备");
        map2.put("value", "233");
        result.add(map2);

        return JSON.toJSONString(result);
    }

}
