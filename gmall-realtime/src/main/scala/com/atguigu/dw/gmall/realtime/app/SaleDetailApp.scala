package com.atguigu.dw.gmall.realtime.app

import java.util

import com.alibaba.fastjson.JSON
import com.atguigu.dw.gmall.common.Constant
import com.atguigu.dw.gmall.realtime.bean.{OrderDetail, OrderInfo, SaleDetail}
import com.atguigu.dw.gmall.realtime.util.{MyKafkaUtil, RedisUtil}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis

import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization

/**
  * Author atguigu
  * Date 2020/2/18 9:31
  */
object SaleDetailApp {
    // 把orderInfo的信息缓存到 redis
    def cacheOrderInfo(client: Jedis, orderInfo: OrderInfo) = {
        val key = "orderInfo_" + orderInfo.id
        cacheToRedis(client, key, orderInfo)
    }
    
    // 把orderDetail的信息缓存到redis
    def cacheOrderDetail(client: Jedis, orderDetail: OrderDetail) = {
        val key = "orderDetail_" + orderDetail.order_id + "_" + orderDetail.id
        cacheToRedis(client, key, orderDetail)
    }
    //
    def cacheToRedis(client: Jedis, key: String, value: AnyRef): Unit ={
        // 需要把value变成json字符串写入到redis
        val content = Serialization.write(value)(DefaultFormats)
//        client.set(key, content)
        client.setex(key, 1000 * 60 * 30, content)  // 给每个key添加一个过期时间
    }
    
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
        
        // 3. join流
        val fullJointSteam = orderInfoStream.fullOuterJoin(orderDetailStream).mapPartitions(it => {
            // 获取一个redis的客户端
            val client: Jedis = RedisUtil.getJedisClient
            // (orderId, (Some(orderInfo), None)) 中会对应多个SaleDetail, 所以需要使用flatMap
            val result: Iterator[SaleDetail] = it.flatMap {
                /*case (orderId, (Some(orderInfo), Some(orderDetail))) =>
                    // 把orderInfo的数据缓存到redis中:  因为orderInfo和orderDetail是一对多的关系
                    cacheOrderInfo(client, orderInfo)
                    
                    import scala.collection.JavaConversions._
                    // 去orderDetail缓存中, 读出与当前这个orderInfo对应的OrderDetail
                    val orderDetailJsonSet: util.Set[String] = client.keys(s"order_detail_${orderInfo.id}_*")
                    val SaleDetailSet = orderDetailJsonSet.map(jsonString => {
                        val orderDetail: OrderDetail = JSON.parseObject(jsonString, classOf[OrderDetail])
                        SaleDetail().mergeOrderInfo(orderInfo).mergeOrderDetail(orderDetail)
                    })
                    
                    // 把同行和缓存中的都封装到saleDetail中
                    SaleDetailSet + SaleDetail().mergeOrderInfo(orderInfo).mergeOrderDetail(orderDetail)
                case (orderId, (Some(orderInfo), None)) =>
                    // 也必须缓存orderInfo
                    cacheOrderInfo(client, orderInfo)
                    import scala.collection.JavaConversions._
                    // 去orderDetail缓存中, 读出与当前这个orderInfo对应的OrderDetail
                    val orderDetailJsonSet: util.Set[String] = client.keys(s"order_detail_${orderInfo.id}_*")
                    val SaleDetailSet = orderDetailJsonSet.map(jsonString => {
                        val orderDetail: OrderDetail = JSON.parseObject(jsonString, classOf[OrderDetail])
                        SaleDetail().mergeOrderInfo(orderInfo).mergeOrderDetail(orderDetail)
                    })
                    SaleDetailSet*/
                case (orederId, (Some(orderInfo), opt)) =>
                    println("some opt....")
                    
                    cacheOrderInfo(client, orderInfo)
                    
                    import scala.collection.JavaConversions._
                    // 去orderDetail缓存中, 读出与当前这个orderInfo对应的OrderDetail
                    val orderDetailJsonSet: util.Set[String] = client.keys(s"orderDetail_${orderInfo.id}_*")
                    val SaleDetailSet = orderDetailJsonSet.map(jsonString => {
                        val orderDetail: OrderDetail = JSON.parseObject(jsonString, classOf[OrderDetail])
                        SaleDetail().mergeOrderInfo(orderInfo).mergeOrderDetail(orderDetail)
                    })
                    
                    
                    if (opt.isDefined) {
                        val orderDetail: OrderDetail = opt.get
                        SaleDetailSet += SaleDetail().mergeOrderInfo(orderInfo).mergeOrderDetail(orderDetail)
                    }
                    SaleDetailSet
                case (orderId, (None, Some(orderDetail))) =>
                    println("none some")
                    // 去orderInfo的缓冲读取数据, 如果读到数据,则orderDetail不需要缓存,否则才需要缓存
                    val orderInfoString: String = client.get(s"orderInfo_${orderDetail.order_id}")
                    if (orderInfoString != null && orderInfoString.startsWith("{")) {
                        val orderInfo: OrderInfo = JSON.parseObject(orderInfoString, classOf[OrderInfo])
                        SaleDetail().mergeOrderDetail(orderDetail).mergeOrderInfo(orderInfo) :: Nil
                        
                    } else {
                        cacheOrderDetail(client, orderDetail)
                        Nil
                    }
            }
            
            client.close()
            result
        })
        
        fullJointSteam.print(10000)
        ssc.start()
        ssc.awaitTermination();
        
    }
    
    
}

/*
缓存orderInfo orderDetail在redis中,
redis的数据类型:
    hash ?
        key                             hash
        "orderInfo"                     field  value
                                        id     orderInfo格式的字符串
                                        
orderInfo
    String
        key                             string
        "orderInfo_" + id               orderInfo对应的json字符串
        
orderDetail:
    String
         key                             string
        "orderDetail_" + orderId + "_"+ id               orderDetail对应的json字符串
        
        
orderInfo
   1
orderDetail
    缓存:
    orderDetail_1_1
    orderDetail_1_2

 */
