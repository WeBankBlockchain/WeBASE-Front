#!/bin/bash

APP_MAIN=com.webank.webase.front.Application
CLASSPATH='conf/:apps/*:lib/*'
CURRENT_DIR=`pwd`
LOG_DIR=${CURRENT_DIR}/log
CONF_DIR=${CURRENT_DIR}/conf

SERVER_PORT=$(cat $CONF_DIR/application.yml| grep "port" | awk '{print $2}'| sed 's/\r//')
if [ ${SERVER_PORT}"" = "" ];then
    echo "$CONF_DIR/application.yml server port has not been configured"
    exit -1
fi

mkdir -p log

startWaitTime=600
processPid=0
processStatus=0
server_pid=0
checkProcess(){
    server_pid=`ps aux | grep java | grep $APP_MAIN | awk '{print $2}'`
    port_pid=`netstat -anp 2>&1|grep $SERVER_PORT|awk '{printf $7}'|cut -d/ -f1`
    if [ -n "$port_pid" ] && [ -n "$(echo $port_pid| sed -n "/^[0-9]\+$/p")" ]; then
        if [[ $server_pid =~ $port_pid ]]; then
            processPid=$port_pid
            processStatus=2
        else
            processPid=$port_pid
            processStatus=1
        fi
    else
        processPid=0
        processStatus=0
    fi
}

JAVA_OPTS=" -Dfile.encoding=UTF-8"
JAVA_OPTS+=" -Xmx256m -Xms256m -Xmn128m -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"
JAVA_OPTS+=" -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}/heap_error.log"

start(){
    checkProcess
    echo "==============================================================================================="
    if [ $processStatus == 1 ]; then
        echo "Port $SERVER_PORT has been occupied by other server PID($processPid)"
        echo "==============================================================================================="
        processStatus=0
    elif [ $processStatus == 2 ]; then
        echo "Server $APP_MAIN Port $SERVER_PORT is running PID($processPid)"
        echo "==============================================================================================="
        processStatus=0
    else
        echo -n "Starting Server $APP_MAIN Port $SERVER_PORT ..."
        nohup $JAVA_HOME/bin/java $JAVA_OPTS -Djava.library.path=$CONF_DIR -cp $CLASSPATH $APP_MAIN >> $LOG_DIR/front.out 2>&1 &
        
        count=1
        result=0
        while [ $count -lt $startWaitTime ] ; do
           checkProcess
           if [ $processPid -ne 0 ]; then
               result=1
               break
           fi
           let count++
           echo -n "."
           sleep 1
       done
        
       if [ $result -ne 0 ]; then
           echo "PID($processPid) [Success]"
           echo "==============================================================================================="
       else
           for subPid in ${server_pid[@]} ; do
               checkResult=`netstat -tunpl 2>&1|grep $subPid|awk '{printf $7}'|cut -d/ -f1`
               if [ -z "$checkResult" ]; then
                   kill -9 $subPid
                   message="Because port $SERVER_PORT not up in $startWaitTime seconds.Script finally killed the process."
               fi
           done
           echo "[Failed]. Please view log file (default path:./log/)."
           echo $message
           echo "==============================================================================================="
       fi
    fi
}

start
