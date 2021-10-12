#!/bin/sh

# start bcos
cd /data && /usr/local/bin/fisco-bcos -c /data/config.ini >>nohup.out &

# start front
cp -r /data/sdk/* /front/conf/
cd /front && bash start.sh

# keep container running
tail -f /docker-start.sh


