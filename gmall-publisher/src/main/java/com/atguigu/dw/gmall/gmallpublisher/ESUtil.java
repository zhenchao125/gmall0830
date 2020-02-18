package com.atguigu.dw.gmall.gmallpublisher;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

/**
 * @Author lzc
 * @Date 2020/2/18 15:36
 */
public class ESUtil {
    static JestClientFactory factory = new JestClientFactory();
    static {
        String esUrl = "http://hadoop102:9200";
        // 1. 创建es客户端的工厂
        HttpClientConfig conf = new HttpClientConfig.Builder(esUrl)
                .maxTotalConnection(100)
                .connTimeout(1000 * 100)
                .readTimeout(1000 * 100)
                .multiThreaded(true)
                .build();
        factory.setHttpClientConfig(conf);
    }

    // 返回一个客户端
    public static JestClient getClient() {
        return factory.getObject();
    }
}
