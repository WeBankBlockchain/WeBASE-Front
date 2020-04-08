#!/bin/bash

APP_MAIN=com.webank.webase.front.Application
CURRENT_DIR=$(pwd)/
CONF_DIR=${CURRENT_DIR}conf

SERVER_PORT=$(cat $CONF_DIR/application.yml | grep "server:" -A 3 | grep "port" | awk '{print $2}'| sed 's/\r//')
if [ ${SERVER_PORT}"" = "" ];then
    echo "$CONF_DIR/application.yml server port has not been configured"
    exit -1
fi

processPid=0
checkProcess(){
    server_pid=$(ps aux | grep java | grep $CURRENT_DIR | grep $APP_MAIN | awk '{print $2}')
    if [ -n "$server_pid" ]; then
        processPid=$server_pid
    else
        processPid=0
    fi
}

stop(){
	checkProcess
	echo "==============================================================================================="
	if [ $processPid -ne 0 ]; then
	    echo -n "Stopping Server $APP_MAIN Port $SERVER_PORT PID($processPid)..."
	    kill -9 $processPid
	    if [ $? -eq 0 ]; then
	        echo "[Success]"
	        echo "==============================================================================================="
	    else
	        echo "[Failed]"
	        echo "==============================================================================================="
	    fi
	else
	    echo "Server $APP_MAIN Port $SERVER_PORT is not running"
	    echo "==============================================================================================="
	fi
}

stop