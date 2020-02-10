package com.atguigu.dw.gmalllogger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dw.gmall.common.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author lzc
 * @Date 2020/2/10 15:24
 */
/*@Controller
@ResponseBody*/
@RestController  // 等价于 @Controller + @ResponseBody
public class LoggerController {

    @PostMapping("/log")
    public String doLog(@RequestParam("log") String log) {
        // 1. 给日志添加一个时间戳, 然后返回新的日志
        log = addTS(log);
        // 2. 把日志落盘(写入到文件) (将来别的程序可以通过flume把数据给采集走)
        save2File(log);
        // 3. 把数据写入到kafka中 : 重要
        send2kafka(log);
        return "ok"; //
    }

    @Autowired  // 对象的自动注入
            KafkaTemplate<String, String> kafka;

    /**
     * 把日志写入到kafka
     * 1. 让sprintboot知道kakka的地址
     * <p>
     * 2. topic怎么分配
     * 启动日志  事件日志
     *
     * @param log
     */
    private void send2kafka(String log) {
        /*if (log.contains("startup")) {
            kafka.send(Constant.STARTUP_TOPIC, log);
        } else {
            kafka.send(Constant.EVENT_TOPIC, log);
        }*/

        String topic = Constant.STARTUP_TOPIC;
        if (log.contains("event")) {
            topic = Constant.EVENT_TOPIC;
        }
        kafka.send(topic, log);
    }

    // 创建一个可以写出日志的Logger对象
    Logger logger = LoggerFactory.getLogger(LoggerController.class);

    /**
     * 把日志存入到文件中
     *
     * @param log
     */
    private void save2File(String log) {
        // 使用log4j把数据写入到文件中  springboot 默认是 logging
        logger.info(log);
    }

    /**
     * 给日志添加时间戳
     *
     * @param log
     * @return
     */
    private String addTS(String log) {
        // 解析json, fastjson
        JSONObject jsonObj = JSON.parseObject(log);
        jsonObj.put("ts", System.currentTimeMillis());
        return jsonObj.toJSONString();
    }
}
