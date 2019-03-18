#!/bin/sh

APP_MAIN=com.webank.webase.front.Application
CLASSPATH='conf/:apps/*:lib/*'
CURRENT_DIR=`pwd`
LOG_DIR=${CURRENT_DIR}/log
CONF_DIR=${CURRENT_DIR}/conf

NODE_DIR=$(cat $CONF_DIR/application.yml| grep "nodeDir" | awk '{print $2}'| sed 's/\r//')
if [ ${NODE_DIR}"" = "" ];then
	echo "$CONF_DIR/application.yml nodeDir is not configured"
	exit -1
fi

rm -rf ${CURRENT_DIR}/conf/ca.crt
if [ -f $NODE_DIR/fisco-data/ca.crt ]; then
	cp $NODE_DIR/fisco-data/ca.crt ${CURRENT_DIR}/conf/
fi
if [ -f $NODE_DIR/webank-data/ca.crt ]; then
	cp $NODE_DIR/webank-data/ca.crt ${CURRENT_DIR}/conf/
fi
if [ -f $NODE_DIR/data/ca.crt ]; then
	cp $NODE_DIR/data/ca.crt ${CURRENT_DIR}/conf/
fi
if [ ! -f ${CURRENT_DIR}/conf/ca.crt ];then
	echo "node's ca.crt is not existed"
	echo "nodeDir:"$NODE_DIR
	exit -1
fi

mkdir -p log
mkdir -p files

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

# start front
startFront
# start report
cd ./report && chmod +x *.sh && dos2unix *.sh && sh report_start.sh