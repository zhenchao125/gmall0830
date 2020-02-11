package com.atguigu.dw.gmall.realtime.bean

import java.text.SimpleDateFormat
import java.util.Date

case class StartupLog(mid: String,
                      uid: String,
                      appId: String,
                      area: String,
                      os: String,
                      channel: String,
                      logType: String,
                      version: String,
                      ts: Long,
                      var logDate: String = null, // 2020-02-11
                      var logHour: String = null) { // 1 2 10 12  小时
    private val f1 = new SimpleDateFormat("yyyy-MM-dd")
    private val f2 = new SimpleDateFormat("HH")
    val d = new Date(ts)
    logDate = f1.format(d)
    logHour = f2.format(d)
}

/*
{"logType":"startup","area":"hebei",
"uid":"7546","os":"ios","appId":"gmall",
"channel":"xiaomi","mid":"mid_351","version":"1.2.0","ts":1581402728428}
 */