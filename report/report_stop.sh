#! /bin/sh

CURRENT_DIR=`pwd`
ReportAgent=${CURRENT_DIR}/ReportAgent.py

agent_pid=`ps aux|grep "$ReportAgent"|grep -v grep|awk '{print $2}'`
kill_cmd="kill -9 ${agent_pid}"

if [ ${agent_pid}"" = "" ];then
    echo "$ReportAgent is not running"
    echo "==============================================================================================="
else
    echo -n "Stopping $ReportAgent (PID=$agent_pid)..."
    eval ${kill_cmd}
    agent_pid=`ps aux|grep "$ReportAgent"|grep -v grep|awk '{print $2}'`
    if [ ${agent_pid}"" = "" ];then
        echo "[Success]"
        echo "==============================================================================================="
    else
        echo "[Failed]"
        echo "==============================================================================================="
    fi
fi

