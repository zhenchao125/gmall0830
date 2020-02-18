package com.atguigu.dw.gmall.gmallpublisher.service;

import com.atguigu.dw.gmall.gmallpublisher.ESUtil;
import com.atguigu.dw.gmall.gmallpublisher.mapper.DauMapper;
import com.atguigu.dw.gmall.gmallpublisher.mapper.OrderMapper;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author lzc
 * @Date 2020/2/12 10:13
 */
@Service
public class PublisherServiceImp implements PublisherService {

    @Autowired
    DauMapper dauMapper;

    @Override
    public Long getDau(String date) {
        // 从数据层读取数据, 然后给Controller使用
        return dauMapper.getDau(date);
    }

    @Override
    public Map<String, Long> getHourDau(String date) {
        /*
        List<Map<hour: 10, count: 100; >
        Map<10: 100;  11: 110>
         */
        List<Map> mapList = dauMapper.getHourDau(date);
        HashMap<String, Long> result = new HashMap<>();
        // 把mapList中的一个Map中的数据取出来, 成为result中的一个key-value
        for (Map map : mapList) {
            String key = (String) map.get("LOGHOUR");
            Long value = (Long) map.get("COUNT");
            result.put(key, value);
        }
        return result;
    }

    @Autowired
    OrderMapper orderMapper;

    @Override
    public Double getTotalAmount(String date) {
        Double total = orderMapper.getTotalAmount(date);
        return total == null ? 0 : total;
    }

    @Override
    public Map<String, Double> getHourAmount(String date) {
        HashMap<String, Double> result = new HashMap<>();
        List<Map> mapList = orderMapper.getHourAmount(date);
        for (Map map : mapList) {
            String key = (String)map.get("CREATE_HOUR");
            Double value = ((BigDecimal) map.get("SUM")).doubleValue();
            result.put(key,value);
        }

        return result;
    }

    // 从es获取聚合结果
    @Override
    public Map<String, Object> getSaleDetailAndAggregationByField(String date,
                                                                  String keyword,
                                                                  String field,
                                                                  int size,
                                                                  int page,
                                                                  int countPerPage) throws IOException {
        // 0.获取查询用的DSL语言
        String dsl = DSLUtil.getDSL(date, keyword, field, size, page, countPerPage);
        // 1. 获取客户端
        JestClient client = ESUtil.getClient();
        Search search = new Search.Builder(dsl)
                .build();
        // 2. 得到查询的结果
        SearchResult searchResult = client.execute(search);

        // Map  "total"-> 1000,  "detail"-> List<Map>  "agg"-> Map("F"->100,"M"->100)
        HashMap<String, Object> result = new HashMap<>();

        // 3. 获取需要的结果
        // 3.1 结果中的总数
        Long total = searchResult.getTotal();
        result.put("total", total);
        // 3.2 获取聚合结果
        Map<String, Long> aggMap = new HashMap<>();
//        System.out.println("searchResult:" + searchResult);
//        System.out.println("searchResult.getAggregations():" + searchResult.getAggregations());
//        System.out.println("\"groupby_\" + field" + "groupby_" + field);

//        System.out.println("searchResult.getAggregations().getTermsAggregation(\"groupby_\" + field):" + searchResult.getAggregations().getTermsAggregation("groupby_" + field));
        List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_" + field).getBuckets();
        for (TermsAggregation.Entry entry : buckets) {
            String key = entry.getKey();
            Long value = entry.getCount();
            aggMap.put(key, value);
        }
        result.put("agg", aggMap);
        // 3.3. 获取详情
        List<Map<String, Object>> detailList = new ArrayList<>();
        List<SearchResult.Hit<HashMap, Void>> hits = searchResult.getHits(HashMap.class);
        for (SearchResult.Hit<HashMap, Void> hit : hits) {
            HashMap source = hit.source;
            detailList.add(source);
        }
        result.put("detail", detailList);

        return result;
    }
}
/*
"aggregations": {
    "groupby_user_agender": {
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 0,
      "buckets": [
        {
          "key": "M",
          "doc_count": 42
        },
        {
          "key": "F",
          "doc_count": 36
        }
      ]
    }
  }




BigDecimal 表示无限精度的浮点数
    Double
    1.389*10^100

    1-0.3==0.7
BigInteger 用来表示无限大的整数
 */
