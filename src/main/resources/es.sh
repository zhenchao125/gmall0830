#!/bin/bash
es_home=/opt/module/elasticsearch-6.3.1
k_home=/opt/module/kibana-6.3.1
case $1 in
start)
    for host in hadoop102 hadoop103 hadoop104 ; do
        ssh $host "source /etc/profile; nohup $es_home/bin/elasticsearch 1>>$es_home/logs/es.log 2>>$es_home/logs/error.log &"
    done

    nohup $k_home/bin/kin/kibana 1>>$k_home/logs/k.log 2>>$k_home/logs/error.log &

;;
stop)
    ps -ef|grep ${k_home} |grep -v grep|awk '{print $2}'|xargs kill
    for host in hadoop102 hadoop103 hadoop104 ; do
        ssh $host "ps -ef|grep $es_home |grep -v grep|awk '{print \$2}'|xargs kill"
    done
;;
help)
    echo "启动es和kibana: "
    echo "    es.sh start"
    echo "停止es和kibanna: "
    echo "    es.sh stop"
;;
* )
    echo "你脚本使用的姿势不对,换个姿势再来"
    echo "启动es和kibana: "
    echo "    es.sh start"
    echo "停止es和kibana: "
    echo "    es.sh stop"
;;
esac


