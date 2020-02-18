package com.atguigu.dw.gmall.gmallpublisher.service

/**
  * Author atguigu
  * Date 2020/2/18 15:44
  */
object DSLUtil {
    
    def getDSL(date: String, keyword: String, field: String, size: Int, page: Int, countPerPage: Int) = {
        s"""
           |{
           |  "query": {
           |    "bool": {
           |      "filter": {
           |        "term": {
           |          "dt": "$date"
           |        }
           |      },
           |      "must": [
           |        {"match": {
           |          "sku_name": {
           |              "query": "$keyword",
           |              "operator": "and"
           |          }
           |        }}
           |      ]
           |    }
           |  },
           |  "aggs": {
           |    "groupby_$field": {
           |      "terms": {
           |        "field": "$field",
           |        "size": $size
           |      }
           |    }
           |  },
           |  "from": ${(page - 1) * countPerPage},
           |  "size": $countPerPage
           |}
        """.stripMargin
    }
}
