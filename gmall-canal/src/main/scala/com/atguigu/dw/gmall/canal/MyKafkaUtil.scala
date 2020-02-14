package com.atguigu.dw.gmall.canal

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

/**
  * Author atguigu
  * Date 2020/2/14 10:48
  */
object MyKafkaUtil {
    private val props = new Properties()
    props.put("bootstrap.servers", "hadoop102:9092,hadoop103:9092,hadoop104:9092")
    // key的序列化
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    // value序列化
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    private val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](props)
    /**
      * 向kafka发送数据
      * @param topic
      * @param content
      */
    def send(topic: String, content: String) = {
        producer.send(new ProducerRecord[String, String](topic, content))
    }
    
}
