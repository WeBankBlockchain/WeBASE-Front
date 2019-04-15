#!/bin/sh

APP_MAIN=com.webank.webase.front.Application
tradePortalPID=0
getTradeProtalPID(){
    javaps=`$JAVA_HOME/bin/jps -l | grep $APP_MAIN`
    if [ -n "$javaps" ]; then
        tradePortalPID=`echo $javaps | awk '{print $1}'`
    else
        tradePortalPID=0
    fi
}

getFrontStatus(){
    getTradeProtalPID
    echo "==============================================================================================="
    if [ $tradePortalPID -ne 0 ]; then
        echo "$APP_MAIN is running(PID=$tradePortalPID)"
        echo "==============================================================================================="
    else
        echo "$APP_MAIN is not running"
        echo "==============================================================================================="
    fi
}

getFrontStatus

cd ./report && sh report_check.sh