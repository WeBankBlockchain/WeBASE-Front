#!/bin/sh

APP_MAIN=com.webank.webase.front.Application
CLASSPATH='conf/:apps/*:lib/*'
CURRENT_DIR=`pwd`
LOG_DIR=${CURRENT_DIR}/log
CONF_DIR=${CURRENT_DIR}/conf

sigarFile="/usr/lib/libsigar-amd64-linux.so"
sigarFile1="/usr/lib/libsigar-x86-linux.so"

 if [ ! -f "sigarFile" ]; then
  sudo cp $CONF_DIR/libsigar-amd64-linux.so $CONF_DIR/libsigar-x86-linux.so  /usr/lib
 fi

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
	    nohup $JAVA_HOME/bin/java -cp $CLASSPATH $APP_MAIN >> $LOG_DIR/front.out 2>&1 &
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
