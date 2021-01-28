#!/usr/bin/env bash

# 在源码编译得到的WeBASE-Front/dist目录中执行

CURRENT_DIR=$(pwd)
CONF_DIR=${CURRENT_DIR}/conf
YML_DIR=${CONF_DIR}/application.yml
SOLCJS_DIR=${CONF_DIR}/solcjs
ENCRYPT_TYPE=""

check_conf_mkdir_dir(){
    if [ ! -d "${CONF_DIR}" ];then
        echo "Conf directory not exist: ${CONF_DIR}! please run command: cp -r conf_template conf"
        exit 1
    elif [ ! -f "${YML_DIR}" ];then
        echo "Yml file not exist: ${YML_DIR}! please run command: cp -r conf_template conf"
        exit 1
    else
        echo "Now make dir ${SOLCJS_DIR}"
        mkdir -p ${SOLCJS_DIR}
    fi

}
check_conf_mkdir_dir

get_encrypt_type(){
    ENCRYPT_TYPE=$(cat $CONF_DIR/application.yml| grep "encryptType" | awk '{print $2}'| sed 's/\r//')
    if [ ${ENCRYPT_TYPE}"" = "" ];then
        echo "$CONF_DIR/application.yml encryptType has not been configured"
        exit 1
    elif [ ${ENCRYPT_TYPE}"" = "0" ];then
        echo "encryptType is ecdsa, ready to download solc 0.6.10.js of ecdsa"
    elif [ ${ENCRYPT_TYPE}"" = "1" ];then
        echo "encryptType is guomi, ready to download solc 0.6.10.js of guomi"
    else
        echo "${ENCRYPT_TYPE} is not correct, only 0 or 1"
        exit 1
    fi
}
get_encrypt_type

get_solc_js(){
    if [ ! -d "${SOLCJS_DIR}" ];then
        echo "Directory not exist: ${SOLCJS_DIR}"
        exit 1
    else
        if [ ${ENCRYPT_TYPE}"" = "0" ];then
            echo "Now download solc 0.6.10.js"
            curl -L https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeBASE/download/solidity/v0.6.10.js -o "${SOLCJS_DIR}"/v0.6.10.js
        elif [ ${ENCRYPT_TYPE}"" = "1" ];then
            echo "Now download solc 0.6.10-gm.js"
            curl -L https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeBASE/download/solidity/v0.6.10-gm.js -o "${SOLCJS_DIR}"/v0.6.10-gm.js
        fi
    fi
}
get_solc_js

exit 0