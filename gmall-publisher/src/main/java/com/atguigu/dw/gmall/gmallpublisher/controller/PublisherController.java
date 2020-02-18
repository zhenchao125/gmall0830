package com.atguigu.dw.gmall.gmallpublisher.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.dw.gmall.gmallpublisher.bean.Option;
import com.atguigu.dw.gmall.gmallpublisher.bean.SaleInfo;
import com.atguigu.dw.gmall.gmallpublisher.bean.Stat;
import com.atguigu.dw.gmall.gmallpublisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

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
    http://localhost:8070/realtime-hour?id=dau&date=2020-02-18
    {"yesterday":{"11":383,"12":123,"17":88,"19":200 },
        "today":{"12":38,"13":1233,"17":123,"19":688 }}

     */
    @GetMapping("/realtime-hour")
    public String getHourTotal(@RequestParam("id") String id,
                               @RequestParam("date") String date) {
        if ("dau".equals(id)) {
            Map<String, Long> today = service.getHourDau(date);
            Map<String, Long> yesterday = service.getHourDau(getYesterday(date));

            HashMap<String, Map<String, Long>> result = new HashMap<>();
            result.put("today", today);
            result.put("yesterday", yesterday);

            return JSON.toJSONString(result);

        } else if ("order_amount".equals(id)) {
            Map<String, Double> today = service.getHourAmount(date);
            Map<String, Double> yesterday = service.getHourAmount(getYesterday(date));

            HashMap<String, Map<String, Double>> result = new HashMap<>();
            result.put("today", today);
            result.put("yesterday", yesterday);

            return JSON.toJSONString(result);
        }


        return null;

    }

    /*
     	http://localhost:8070/sale_detail?date=2020-02-18&&startpage=1&&size=5&&keyword=手机小米
     */
    @GetMapping("/sale_detail")
    public String getSaleDetail(@RequestParam("date") String date,
                                @RequestParam("startpage") int page,
                                @RequestParam("size") int countPerPage,
                                @RequestParam("keyword") String keyword) throws IOException {
        // 1. 获取聚合结果
        Map<String, Object> resultAge = service.getSaleDetailAndAggregationByField(date, keyword, "user_age", 100, page, countPerPage);
        Map<String, Object> resultGender = service.getSaleDetailAndAggregationByField(date, keyword, "user_gender", 2, page, countPerPage);

        // 2. 封装数据
        SaleInfo saleInfo = new SaleInfo();
        // 2.1 赋值总数
        long total = (long)resultAge.get("total");
        saleInfo.setTotal(total);
        // 2.2 赋值明细
        List<Map<String, Object>> detail = (List<Map<String, Object>>)resultAge.get("detail");
        saleInfo.setDetail(detail);
        // 2.3 两个饼图
        // 2.3.1 性别的饼图
        Stat genderStat = new Stat();
        genderStat.setTitle("用户性别占比");
        Map<String, Long> genderAgg = (Map<String, Long>) resultGender.get("agg");
        Set<Map.Entry<String, Long>> genderEntries = genderAgg.entrySet();
        for (Map.Entry<String, Long> genderEntry : genderEntries) {
            Option option = new Option();
            option.setName(genderEntry.getKey().replace("F", "女").replace("M", "男"));
            option.setValue(genderEntry.getValue());
            genderStat.addOption(option);
        }
        saleInfo.addStat(genderStat);
        //2.3.2 年龄的饼图
        Stat ageStat = new Stat();
        ageStat.addOption(new Option("20岁以下", 0));
        ageStat.addOption(new Option("20岁到30岁", 0));
        ageStat.addOption(new Option("30岁及30岁以上", 0));

        // 关于年龄的聚合结果
        Map<String, Long> ageAgg = (Map<String, Long>) resultAge.get("agg");
        Set<Map.Entry<String, Long>> entries = ageAgg.entrySet();
        for (Map.Entry<String, Long> entry : entries) {
            int age = Integer.parseInt(entry.getKey());
            Long value = entry.getValue();
            if(age < 20){
                Option o0 = ageStat.getOptions().get(0);
                o0.setValue(o0.getValue() + value);
            }else if(age < 30){
                Option o1 = ageStat.getOptions().get(1);
                o1.setValue(o1.getValue() + value);
            }else {
                Option o2 = ageStat.getOptions().get(2);
                o2.setValue(o2.getValue() + value);
            }
        }
        saleInfo.addStat(ageStat);

        return JSON.toJSONString(saleInfo);
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
