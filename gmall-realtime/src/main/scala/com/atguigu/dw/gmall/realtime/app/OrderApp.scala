package com.atguigu.dw.gmall.realtime.app

import com.alibaba.fastjson.JSON
import com.atguigu.dw.gmall.common.Constant
import com.atguigu.dw.gmall.realtime.util.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Author atguigu
  * Date 2020/2/14 11:20
  */
object OrderApp {
    def main(args: Array[String]): Unit = {
        val conf: SparkConf = new SparkConf().setAppName("OrderApp").setMaster("local[2]")
        val ssc: StreamingContext = new StreamingContext(conf, Seconds(3))
        // 1. 从kafka消费数据
        val sourceStream: InputDStream[(String, String)] = MyKafkaUtil.getKafkaStream(ssc, Constant.ORDER_TOPIC)
        // 2. 对数据做封装(样例类)
        val orderInfoStream: DStream[OrderInfo] = sourceStream.map {
            case (_, jsonString) =>
                JSON.parseObject(jsonString, classOf[OrderInfo])
        }
        
        //        orderInfoStream.print(10000)
        // 3. 写入到hbase中(phoenix)
        import org.apache.phoenix.spark._
        orderInfoStream.foreachRDD(rdd => {
            rdd.saveToPhoenix("GMALL0830_ORDER_INFO",
                Seq("ID", "PROVINCE_ID", "CONSIGNEE", "ORDER_COMMENT", "CONSIGNEE_TEL", "ORDER_STATUS", "PAYMENT_WAY", "USER_ID", "IMG_URL", "TOTAL_AMOUNT", "EXPIRE_TIME", "DELIVERY_ADDRESS", "CREATE_TIME", "OPERATE_TIME", "TRACKING_NO", "PARENT_ORDER_ID", "OUT_TRADE_NO", "TRADE_BODY", "CREATE_DATE", "CREATE_HOUR"),
                zkUrl = Some("hadoop102,hadoop103,hadoop104:2181"))
        })
        ssc.start()
        ssc.awaitTermination()
    }
}
