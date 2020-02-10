package com.atguigu.dw.gmalllogger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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

        return "ok"; // abc.html
    }

    /**
     * 把日志存入到文件中
     * @param log
     */
    private void save2File(String log) {
        // 使用log4j把数据写入到文件中
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
