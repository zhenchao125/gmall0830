package com.atguigu.dw.gmall.realtime.app

import com.alibaba.fastjson.JSON
import com.atguigu.dw.gmall.common.Constant
import com.atguigu.dw.gmall.realtime.bean.StartupLog
import com.atguigu.dw.gmall.realtime.util.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Author atguigu
  * Date 2020/2/11 14:13
  */
object DauApp {
    def main(args: Array[String]): Unit = {
        // 1. 从kafka读取数据
        val conf: SparkConf = new SparkConf().setAppName("DauApp").setMaster("local[2]")
        val ssc: StreamingContext = new StreamingContext(conf, Seconds(3))
        
        val rawStream: InputDStream[(String, String)] = MyKafkaUtil.getKafkaStream(ssc, Constant.STARTUP_TOPIC)
        // 2. 把数据解析, 然后封装在样例类中
        val startupLogStream: DStream[StartupLog] = rawStream.map {
            case (_, v) => JSON.parseObject(v, classOf[StartupLog])
        }
        startupLogStream.print
        
        // 启动流
        ssc.start()
        ssc.awaitTermination()
    }
}
