package com.atguigu.dw.gmall.gmallpublisher.service;

import com.atguigu.dw.gmall.gmallpublisher.mapper.DauMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
