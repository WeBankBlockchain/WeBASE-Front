#! /bin/sh

CURRENT_DIR=`pwd`
ReportAgent=${CURRENT_DIR}/ReportAgent.py
LOG_DIR=${CURRENT_DIR}/log
mkdir -p $LOG_DIR

existed=`ps aux|grep "$ReportAgent"|grep -v grep|awk '{print $2}'`
if [ ! ${existed}"" = "" ];then
    echo "$ReportAgent is running..."
    echo "==============================================================================================="
else
    echo -n "Starting $ReportAgent "
    nohup python -u $ReportAgent >> $LOG_DIR/report.out 2>&1  &
    sleep 5
    agent_pid=`ps aux|grep "$ReportAgent"|grep -v grep|awk '{print $2}'`
    if [ ! ${agent_pid}"" = "" ];then
        echo "(PID=$agent_pid)...[Success]"
        echo "==============================================================================================="
    else
        echo "[Failed]"
        echo "==============================================================================================="
    fi
fi