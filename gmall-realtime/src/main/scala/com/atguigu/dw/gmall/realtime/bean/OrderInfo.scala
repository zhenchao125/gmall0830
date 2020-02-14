package com.atguigu.dw.gmall.realtime.bean

case class OrderInfo(id: String,
                     province_id: String,
                     // 敏感做脱敏处理
                     var consignee: String,
                     order_comment: String,
                     var consignee_tel: String,
                     order_status: String,
                     payment_way: String,
                     user_id: String,
                     img_url: String,
                     total_amount: Double,
                     expire_time: String,
                     delivery_address: String,
                     create_time: String,  // 2019-11-11 01:01:01
                     operate_time: String,
                     tracking_no: String,
                     parent_order_id: String,
                     out_trade_no: String,
                     trade_body: String,
                     var create_date: String = null, // 2020-02-14
                     var create_hour: String = null) { // "10"  "11"
    // 1. 对需要脱敏的数据进行处理  李振超  李**   186****1634
    consignee = consignee.substring(0, 1) + "**"
    consignee_tel = consignee_tel.substring(0, 3) + "****" + consignee_tel.substring(7, 11)
    //    consignee_tel = consignee_tel.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1****$3")
    // 2. 计算下create_date 和 create_hour
    create_date = create_time.substring(0, 10)
    create_hour = create_time.substring(11, 13)
}
