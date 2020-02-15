package com.atguigu.dw.gmall.realtime.bean

case class AlertInfo(mid: String,
                     uids: java.util.HashSet[String],  // 领取优惠券的用户
                     itemIds: java.util.HashSet[String], // 那些商品的优惠券被领取
                     events: java.util.List[String], // 所有的事件
                     ts: Long) //时间戳

