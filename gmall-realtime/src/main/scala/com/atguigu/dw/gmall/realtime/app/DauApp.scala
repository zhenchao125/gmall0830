package com.atguigu.dw.gmall.realtime.app

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.alibaba.fastjson.JSON
import com.atguigu.dw.gmall.common.Constant
import com.atguigu.dw.gmall.realtime.bean.StartupLog
import com.atguigu.dw.gmall.realtime.util.{MyKafkaUtil, RedisUtil}
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis
import org.apache.phoenix.spark._

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
        
        // 3. 去重
        // 3.1 先从redis中读取语句启动的记录, 把启动的过滤掉
        val filteredStream = startupLogStream.transform(rdd => {
            // 3.2 读取redis中已经启动的记录
            val client: Jedis = RedisUtil.getJedisClient
            // topic_startup:2020-02-11
            val midSet: util.Set[String] = client.smembers(Constant.STARTUP_TOPIC + ":" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
            client.close()
            val bd: Broadcast[util.Set[String]] = ssc.sparkContext.broadcast(midSet)
            // 3.3 过滤掉那些已经启动过的设备
            rdd
                .filter(log => {
                    !bd.value.contains(log.mid)
                })
                .map(log => (log.mid, log))
                .groupByKey
                .map{
                    // mid_3 启动3次:  100s 200s 300s
//                    case (_, logIt) => logIt.toList.sortBy(_.ts).head
                    case (_, logIt) => logIt.toList.minBy(_.ts)
                }
            
            // 在一个时间周期内, 一个设备启动了两次  mid ...
        })
        
        // 3.4 把第一次启动的设备的mid写入到redis
        filteredStream.foreachRDD(rdd => {
            rdd.foreachPartition(logIt => {
                // 获取redis连接
                val client: Jedis = RedisUtil.getJedisClient
                // 写mid到redis中
                logIt.foreach(log => {
                    // 一次写一个
                    client.sadd(Constant.STARTUP_TOPIC + ":" + log.logDate, log.mid)
                })
                // 关闭连接
                client.close()
            })
        })
        filteredStream.print()
        // 4. 写入hbase
        filteredStream.foreachRDD(rdd => {
            // 1. 提前在phoenix中创建要保存数据的表
            
            // 2. 直接保存
            rdd.saveToPhoenix(
                "GMALL0830_DAU",
                Seq("MID", "UID", "APPID", "AREA", "OS", "CHANNEL", "LOGTYPE", "VERSION", "TS", "LOGDATE", "LOGHOUR"),
                zkUrl = Some("hadoop102,hadoop103,hadoop104:2181")
            )
        })
        
        // 启动流
        ssc.start()
        ssc.awaitTermination()
    }
}

/*

数据怎么处理?

1. 最终要存储的数据是 每个设备的当日的第一次启动的明细(第一次启动的日志信息)

2. 一台设备可能会每天启动多次, 我们只保留第一次启动:  去重

3. 借助于 redis来实现去重:  Set
    
    a: 把启动的mid写入到 redis的Set
           启动:           mid_1  mid_2  mid_3  mid_1
           redis-> Set:    mid_1  mid_2  mid_3
           
    b: 所有的信息, 过滤, 如果redis中不存在, 则表示这个设备是第一次启动, 然后这条记录写入到hbase中

 */