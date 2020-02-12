package com.atguigu.dw.gmall.gmallpublisher.service;

import com.atguigu.dw.gmall.gmallpublisher.mapper.DauMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author lzc
 * @Date 2020/2/12 10:13
 */
@Service
public class PublisherServiceImp implements PublisherService {

    @Autowired
    DauMapper dauMapper;
    @Override
    public Long getDau(String date) {
        // 从数据层读取数据, 然后给Controller使用
        return dauMapper.getDau(date);
    }

    @Override
    public Map<String, Long> getHourDau(String date) {
        /*
        List<Map<hour: 10, count: 100; >
        Map<10: 100;  11: 110>
         */
        List<Map> mapList = dauMapper.getHourDau(date);
        HashMap<String, Long> result = new HashMap<>();
        // 把mapList中的一个Map中的数据取出来, 成为result中的一个key-value
        for (Map map : mapList) {
            String key = (String)map.get("LOGHOUR");
            Long value = (Long) map.get("COUNT");
            result.put(key, value);
        }
        return result;
    }
}
