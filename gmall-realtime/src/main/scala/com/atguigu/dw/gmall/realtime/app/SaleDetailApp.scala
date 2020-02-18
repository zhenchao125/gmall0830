package com.atguigu.dw.gmall.realtime.app

import com.alibaba.fastjson.JSON
import com.atguigu.dw.gmall.common.Constant
import com.atguigu.dw.gmall.realtime.bean.{OrderDetail, OrderInfo}
import com.atguigu.dw.gmall.realtime.util.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Author atguigu
  * Date 2020/2/18 9:31
  */
object SaleDetailApp {
    def main(args: Array[String]): Unit = {
        // 1. 从kafka读取数据
        val conf: SparkConf = new SparkConf().setAppName("DauApp").setMaster("local[2]")
        val ssc: StreamingContext = new StreamingContext(conf, Seconds(3))
        // 2. 读取order_detail 和order_info 相关的流
        val orderInfoStream: DStream[(String, OrderInfo)] = MyKafkaUtil.getKafkaStream(ssc, Constant.ORDER_TOPIC).map {
            case (_, jsonString) =>
                val orderInfo: OrderInfo = JSON.parseObject(jsonString, classOf[OrderInfo])
                (orderInfo.id, orderInfo)
        }
        val orderDetailStream: DStream[(String, OrderDetail)] = MyKafkaUtil.getKafkaStream(ssc, Constant.DETAIL_TOPIC).map {
            case (_, jsonString) =>
                val orderDetail: OrderDetail = JSON.parseObject(jsonString, classOf[OrderDetail])
                (orderDetail.order_id, orderDetail)
        }
        
        // join流
        
        
        orderInfoStream.print(1000)
        orderDetailStream.print(1000)
        
        ssc.start()
        ssc.awaitTermination();
        
    }
}
