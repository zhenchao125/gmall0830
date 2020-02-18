package com.atguigu.dw.gmall.canal

import java.util

import com.alibaba.fastjson.JSONObject
import com.alibaba.otter.canal.protocol.CanalEntry
import com.alibaba.otter.canal.protocol.CanalEntry.EventType
import com.atguigu.dw.gmall.common.Constant
import org.apache.kafka.common.internals.Topic

/**
  * Author atguigu
  * Date 2020/2/14 10:10
  */
object CanalHandler {
    
    import scala.collection.JavaConversions._
    
    // 对数据进行解析
    def handle(tableName: String, rowDatas: util.List[CanalEntry.RowData], eventType: CanalEntry.EventType) = {
        if ("order_info" == tableName && rowDatas != null && !rowDatas.isEmpty && eventType == EventType.INSERT) {
            sendRowDataToKafka(rowDatas, Constant.ORDER_TOPIC)
        }else if("order_detail" == tableName && rowDatas != null && !rowDatas.isEmpty && eventType == EventType.INSERT){
            sendRowDataToKafka(rowDatas, Constant.DETAIL_TOPIC)
        }
    }
    
    /**
      * 把数据发送到指定的topic中
      * @param rowDatas
      * @param topic
      */
    private def sendRowDataToKafka(rowDatas: util.List[CanalEntry.RowData], topic: String): Unit = {
        for (rowData <- rowDatas) {
            val jsonObj = new JSONObject()
            // 获取到变化后的所有的列
            val columns: util.List[CanalEntry.Column] = rowData.getAfterColumnsList
            for (column <- columns) {
                val key: String = column.getName // 获取列名
                val value: String = column.getValue // 获取列值
                jsonObj.put(key, value)
            }
            // 得到一个kafka的生产者, 通过生成这向kafka写数据
            MyKafkaUtil.send(topic, jsonObj.toJSONString)
        }
    }
}

/*
==
equals
    在scala中  == 等价于 equals
eq
    等价于java中的 ==
    对对象类型来说, 用来判断两个对象的地址值是否相等



 */