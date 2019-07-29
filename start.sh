#!/bin/sh

APP_MAIN=com.webank.webase.front.Application
CLASSPATH='conf/:apps/*:lib/*'
CURRENT_DIR=`pwd`
LOG_DIR=${CURRENT_DIR}/log
CONF_DIR=${CURRENT_DIR}/conf

mkdir -p log

tradePortalPID=0
getTradeProtalPID(){
    javaps=`$JAVA_HOME/bin/jps -l | grep $APP_MAIN`
    if [ -n "$javaps" ]; then
        tradePortalPID=`echo $javaps | awk '{print $1}'`
    else
        tradePortalPID=0
    fi
}

startFront(){
	getTradeProtalPID
	echo "==============================================================================================="
	if [ $tradePortalPID -ne 0 ]; then
	    echo "$APP_MAIN is already started(PID=$tradePortalPID)"
	    echo "==============================================================================================="
	else
	    echo -n "Starting $APP_MAIN "
	    nohup $JAVA_HOME/bin/java -Djava.library.path=$CONF_DIR -cp $CLASSPATH $APP_MAIN >> $LOG_DIR/front.out 2>&1 &
	    sleep 5
	    getTradeProtalPID
	    if [ $tradePortalPID -ne 0 ]; then
	        echo "(PID=$tradePortalPID)...[Success]"
	        echo "==============================================================================================="
	    else
	        echo "[Failed]"
	        echo "==============================================================================================="
	    fi
	fi
}
startFront
