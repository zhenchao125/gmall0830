package com.atguigu.dw.gmall.gmallpublisher.mapper;

import java.util.List;
import java.util.Map;

public interface OrderMapper {
    Double getTotalAmount(String date);

    List<Map> getHourAmount(String date);
}
