package com.atguigu.dw.gmall.realtime.app

import java.util

import com.alibaba.fastjson.JSON
import com.atguigu.dw.gmall.common.Constant
import com.atguigu.dw.gmall.realtime.bean.{AlertInfo, EventLog}
import com.atguigu.dw.gmall.realtime.util.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}

/**
  * Author atguigu
  * Date 2020/2/15 9:13
  */
object AlterApp {
    def main(args: Array[String]): Unit = {
        val conf: SparkConf = new SparkConf().setAppName("DauApp").setMaster("local[2]")
        val ssc: StreamingContext = new StreamingContext(conf, Seconds(5))
        // 1. 从kakfa读取数据, 并添加窗口: 窗口长度5分组, 滑动步长5s
        val rawStream = MyKafkaUtil.getKafkaStream(ssc, Constant.EVENT_TOPIC).window(Minutes(5))
        
        // 2. 封装数据
        val eventLogStream = rawStream.map {
            case (_, jsonString) =>
                val log: EventLog = JSON.parseObject(jsonString, classOf[EventLog])
                (log.mid, log)
            
        }
        // 3. 处理数据(根据分析来完成逻辑编码)
        // 3.1 按照mid'分组
        val midLogGrouped: DStream[(String, Iterable[EventLog])] = eventLogStream.groupByKey
        // 3.2 产生预警信息
        val alertInfoStream = midLogGrouped.map {
            case (mid, logIt) =>
                // logIt 在这最近的5分钟内, 在mid这个设备上的所有事件日志
                // 记录: 1. 5分钟内当前设备有几个用户登录   2. 有没有点击(浏览商品)
                // 返回: 预警信息
                // 1. 记录领取过优惠券的用户  (java的set集合, scala的集合到es中看不到数据)
                val uidSet = new util.HashSet[String]()
                // 2. 记录5分钟内所有的事件
                val eventList: util.List[String] = new util.ArrayList[String]()
                // 3. 记录领取的优惠券对应的商品的id
                val itemSet = new util.HashSet[String]()
                
                var isClickItem = false // 表示5分钟内有没有点击商品
                import scala.util.control.Breaks._
                breakable {
                    logIt.foreach(log => {
                        eventList.add(log.eventId) // 把事件保存下来
                        // 如果事件类型是优惠券, 表示有一个用户领取了优惠券, 把这个用户保存下来
                        log.eventId match {
                            case "coupon" =>
                                uidSet.add(log.uid) // 把领取优惠券的用户存储
                                itemSet.add(log.itemId) // 把优惠券对应的商品id存储
                            
                            case "clickItem" =>
                                isClickItem = true // 表示与用户点击了商品
                                break
                            case _ =>
                        }
                    })
                }
                
                (uidSet.size() >= 3 && !isClickItem, AlertInfo(mid, uidSet, itemSet, eventList, System.currentTimeMillis()))
        }
        // 4. 把key是true的预警信息写入到es中
        alertInfoStream.filter(_._1).foreachRDD(rdd => {
//            rdd.saveToES
        })
        
        alertInfoStream.print(1000)
        ssc.start()
        ssc.awaitTermination()
        
        
    }
}

/*
1. 从 kakfa 消费数据

2. 对事件日志封装成相应的样例类

3. 对事件分析, 然后生成预警信息
    同一设备，5分钟内三次及以上用不同账号登录并领取优惠劵，
    并且在登录到领劵过程中没有浏览商品。
    同时达到以上要求则产生一条预警日志。
    同一设备，每分钟只记录一次预警。
    
    1. 同一个设备    按照设备分组
    2. 5分钟内    Window
    3. 三次及以上用不同账号登录 统计用户的数量(聚合)
    4. 领取优惠劵 过滤
    5. 没有浏览商品  事件中没有浏览商品的事件
    ---
    6. 同一设备，每分钟只记录一次预警。(用 es本身的功能实现)

4. 把预警信息写入到es
 


 */