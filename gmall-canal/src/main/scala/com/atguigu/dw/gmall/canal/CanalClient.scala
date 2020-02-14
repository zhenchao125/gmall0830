package com.atguigu.dw.gmall.canal

import java.net.{InetSocketAddress, SocketAddress}
import java.util

import com.alibaba.otter.canal.client.{CanalConnector, CanalConnectors}
import com.alibaba.otter.canal.protocol.CanalEntry.{EntryType, RowChange}
import com.alibaba.otter.canal.protocol.{CanalEntry, Message}
import com.google.protobuf.ByteString


/**
  * Author atguigu
  * Date 2020/2/14 9:14
  */
object CanalClient {
    def main(args: Array[String]): Unit = {
        // 可以把java的集合转换成scala的集合, 这样就可以对java的集合使用scala的遍历语法
        import scala.collection.JavaConversions._
        
        val address: SocketAddress = new InetSocketAddress("hadoop102", 11111)
        // 1. 创建一个canal的客户端连接器
        val connector: CanalConnector = CanalConnectors.newSingleConnector(address, "example", "", "")
        // 2. 连接到canal实例
        connector.connect()
        // 订阅数据, example实例实际监控了很多的数据, 但是我们的应用只需要其中的一部分
        connector.subscribe("gmall0830.*") // 获取gmall0830这个数据库下的所有的表
        // 3. 获取数据
        while (true) { // 轮询的方式从canal来获取数据
            val msg: Message = connector.get(100) // 100 表示最多有多少条sql语句导致的变化的信息
            val entries: util.List[CanalEntry.Entry] = if (msg != null) msg.getEntries else null
            
            if (entries != null && !entries.isEmpty) {
                // 实体
                for (entry <- entries) {
                    // Entry的类型必须是RowData 不能是事务的开始, 结束等其他类型
                    if (entry != null && entry.getEntryType == EntryType.ROWDATA) {
                        val storeValue: ByteString = entry.getStoreValue
                        val rowChange: RowChange = RowChange.parseFrom(storeValue)
                        
                        val rowDatas: util.List[CanalEntry.RowData] = rowChange.getRowDatasList
                        // 1. 表名  2. 每行的数据  3. RowChange的事件类型(insert , update, delete,...)
                        CanalHandler.handle(entry.getHeader.getTableName, rowDatas, rowChange.getEventType)
                    }
                }
            } else {
                println("没有拉取到数据, 2s后继续...")
                Thread.sleep(2000)
            }
        }
    }
}

/*

1. 从canal服务器读取数据   order_info
    
    1. 创建一个canal的客户端
    
    2. 订阅数据
    
    3. 连接到canal服务器
    
    4. 服务器返回数据, 客户端对返回的数据做解析  难点
        数据封装了很多层, 需要一层层的展开
        Message 一次拉一个Message, 一个Message可以看成是由多条sql语句执行的结果
        Entry  实体, 一个Message会封装多个Entry , 一个Entry可以看成是由1 条sql执行的结果   (会对表中的多行数据产生影响)
            StoreValue 存储的值, 一个Entry内部封装了一个StoreValue, 序列化的值
        RowChange 行变化. 一个SoreValue中有1个RowChange, 表示一个sql导致的多行的数据的变化
            RowData 行数据. 一个RowChange中封装了多个RowData, 一个RowData表示一行变化后的数据
        Column 列. 一个RowData中会封装多个Column
            列名和列值

2. 把读到数据变成json字符串写入到kafka中
        {"c1": "v1", "c2": "v2", ...  }

* */
