#! /bin/sh

CURRENT_DIR=`pwd`
ReportAgent=${CURRENT_DIR}/ReportAgent.py

agent_pid=`ps aux|grep "$ReportAgent"|grep -v grep|awk '{print $2}'`

if [ ${agent_pid}"" = "" ];then
    echo "$ReportAgent is not running"
    echo "==============================================================================================="
else
    echo "$ReportAgent is running(PID=$agent_pid)"
    echo "==============================================================================================="
fi

