#!/bin/bash
gmall0830=/opt/gmall0830
case $1 in
start)
    for host in hadoop102 hadoop103 hadoop104 ; do
        ssh $host "source /etc/profile ; nohup java -jar $gmall0830/gmall-logger-0.0.1-SNAPSHOT.jar 1>>$gmall0830/gmall.log  2>>$gmall0830/gmall.err &"
    done

;;
stop)
    for host in hadoop102 hadoop103 hadoop104 ; do
        ssh $host "source /etc/profile ; jps | grep gmall-logger-0.0.1-SNAPSHOT.jar | awk {'print \$1'} | xargs kill -9"
    done
;;
help)
    echo "启动日志服务: "
    echo "    log_all start"
    echo "停止日志服务: "
    echo "    log_all stop"
;;
* )
    echo "你脚本使用的姿势不对,换个姿势再来"
    echo "启动日志服务: "
    echo "    log_all start"
    echo "停止日志服务: "
    echo "    log_all stop"
;;
esac


# jps | grep gmall-logger-0.0.1-SNAPSHOT.jar | awk {'print $1'} | xargs kill -9
# jps | awk '/gmall-logger-0.0.1-SNAPSHOT.jar/ {print $1}' | xargs -9 kill