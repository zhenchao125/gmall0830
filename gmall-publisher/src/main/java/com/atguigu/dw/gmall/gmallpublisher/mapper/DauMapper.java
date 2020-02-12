package com.atguigu.dw.gmall.gmallpublisher.mapper;

public interface DauMapper {
    // 方法内部到底执行什么样的sql, 需要去写xml文件, 在xml文件中定义sql语句
    Long getDau(String date);
}
