package com.atguigu.dw.gmall.realtime.util

import com.atguigu.dw.gmall.realtime.bean.AlertInfo
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.core.{Bulk, Index}
import org.apache.spark.rdd.RDD

/**
  * Author atguigu
  * Date 2020/2/17 15:09
  */
object EsUtil {
    val esUrl = "http://hadoop102:9200"
    // 1. 创建es客户端的工厂
    val factory = new JestClientFactory
    var conf: HttpClientConfig = new HttpClientConfig.Builder(esUrl)
        .maxTotalConnection(100)
        .connTimeout(1000 * 100)
        .readTimeout(1000 * 100)
        .multiThreaded(true)
        .build()
    factory.setHttpClientConfig(conf)
    
    // 返回一个客户端
    def getClient: JestClient = factory.getObject
    
    /*
          1.   {  }
          
          2. ({}, "")
     */
    /*def insertBulk(index: String, sources: Iterable[Any]){
        val client: JestClient = getClient
        val bulkBuilder: Bulk.Builder = new Bulk.Builder()
            .defaultIndex(index)
            .defaultType("_doc")
        sources.foreach(source => {
            source match {
                case (s, id:String) =>
                    val action: Index =  new Index.Builder(s).id(id).build()
                    bulkBuilder.addAction(action)
                case s =>
                    val action: Index =  new Index.Builder(s).build()
                    bulkBuilder.addAction(action)
            }
            
           
        })
        client.execute(bulkBuilder.build())
        client.shutdownClient()
    }*/
    
    // 批量插入
    def insertBulk(index: String, sources: TraversableOnce[Any]) {
        val client: JestClient = getClient
        val bulkBuilder: Bulk.Builder = new Bulk.Builder()
            .defaultIndex(index)
            .defaultType("_doc")
        sources.foreach {
            case (s, id: String) =>
                val action: Index = new Index.Builder(s).id(id).build()
                bulkBuilder.addAction(action)
            case s =>
                val action: Index = new Index.Builder(s).build()
                bulkBuilder.addAction(action)
        }
        client.execute(bulkBuilder.build())
        client.shutdownClient()
    }
    
    /**
      * 向es插入单条数据
      *
      * @param index
      * @param source
      * @param id
      */
    def insertSingle(index: String, source: Any, id: String = null) {
        val client: JestClient = getClient
        val action = new Index.Builder(source)
            .index(index)
            .`type`("_doc")
            .id(id)
            .build()
        client.execute(action)
        client.shutdownClient()
    }
    
    // RDD转换成 ESFunction
    implicit class ESFunction(rdd: RDD[AlertInfo]) {
        def saveToES(index: String): Unit = {
            rdd.foreachPartition((alertInfoIt: Iterator[AlertInfo]) => {
                val result: Iterator[(AlertInfo, String)] = alertInfoIt.map(info => {
                    // 如果不拼接mid, 会导致不同设备的预警信息互相覆盖
                    (info, info.mid + "_" + info.ts / 1000 / 60) //
                })
                EsUtil.insertBulk("gmall0830_coupon_alert", result)
            })
        }
    }
    
}

/*
向es插入数据:
    1. 有一个能够连接件到es客户端
    
    2. 通过es客户端向es插入数据
 */