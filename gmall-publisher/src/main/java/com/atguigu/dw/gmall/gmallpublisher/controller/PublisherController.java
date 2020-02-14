package com.atguigu.dw.gmall.gmallpublisher.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.dw.gmall.gmallpublisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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
    public String getTotal(@RequestParam("date") String date) {
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

        HashMap<String, String> map3 = new HashMap<>();
        map3.put("id", "order_amount");
        map3.put("name", "新增交易额");
        map3.put("value", service.getTotalAmount(date) + "");
        result.add(map3);

        return JSON.toJSONString(result);
    }

    /*
    http://localhost:8070/realtime-hour?id=dau&date=2020-02-11
    {"yesterday":{"11":383,"12":123,"17":88,"19":200 },
        "today":{"12":38,"13":1233,"17":123,"19":688 }}

     */
    @GetMapping("/realtime-hour")
    public String getHourCount(@RequestParam("id") String id
            , @RequestParam("date") String date) {
        if ("dau".equals(id)) {
            Map<String, Long> today = service.getHourDau(date);
            Map<String, Long> yesterday = service.getHourDau(getYesterday(date));

            HashMap<String, Map<String, Long>> result = new HashMap<>();
            result.put("today", today);
            result.put("yesterday", yesterday);

            return JSON.toJSONString(result);

        } else if ("".equals(id)) {

        }


        return null;

    }

    /**
     * 计算昨天的年月日
     *
     * @param date
     * @return 2020-02-11
     */
    private String getYesterday(String date) {

        return LocalDate.parse(date).minusDays(1).toString();
    }
}
