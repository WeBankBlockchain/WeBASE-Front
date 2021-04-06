#!/usr/bin/env bash

# 在源码编译得到的WeBASE-Front/dist目录中执行

CURRENT_DIR=$(pwd)
#CONF_DIR=${CURRENT_DIR}/conf
STATIC_JS_DIR=${CURRENT_DIR}/static/static/js
#SOLCJS_DIR=${CONF_DIR}/solcjs
#
#check_conf_mkdir_dir(){
#    if [ ! -d "${CONF_DIR}" ];then
#        echo "Conf directory not exist: ${CONF_DIR}! please run command: cp -r conf_template conf"
#        exit 1
#    elif [ ! -f "${YML_DIR}" ];then
#        echo "Yml file not exist: ${YML_DIR}! please run command: cp -r conf_template conf"
#        exit 1
#    else
#        echo "Now make dir ${SOLCJS_DIR}"
#        mkdir -p ${SOLCJS_DIR}
#    fi
#
#}
#check_conf_mkdir_dir

get_solc_js(){
    if [ ! -d "${STATIC_JS_DIR}" ];then
        echo "Directory not exist: ${STATIC_JS_DIR}"
        exit 1
    else
        echo "Now download all solidity js in ${STATIC_JS_DIR}, if offline, download js file and put them in this dir"
        if [[ ! -f "${STATIC_JS_DIR}/v0.4.25.js" ]]; then
            curl -L https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeBASE/download/solidity/v0.4.25.js -o "${STATIC_JS_DIR}"/v0.4.25.js
        fi
        if [[ ! -f "${STATIC_JS_DIR}/v0.4.25-gm.js" ]]; then
            curl -L https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeBASE/download/solidity/v0.4.25-gm.js -o "${STATIC_JS_DIR}"/v0.4.25-gm.js
        fi
        if [[ ! -f "${STATIC_JS_DIR}/v0.5.1.js" ]]; then
            curl -L https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeBASE/download/solidity/v0.5.1.js -o "${STATIC_JS_DIR}"/v0.5.1.js
        fi
        if [[ ! -f "${STATIC_JS_DIR}/v0.5.1-gm.js" ]]; then
            curl -L https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeBASE/download/solidity/v0.5.1-gm.js -o "${STATIC_JS_DIR}"/v0.5.1-gm.js
        fi
        if [[ ! -f "${STATIC_JS_DIR}/v0.6.10.js" ]]; then
            curl -L https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeBASE/download/solidity/v0.6.10.js -o "${STATIC_JS_DIR}"/v0.6.10.js
        fi
        if [[ ! -f "${STATIC_JS_DIR}/v0.6.10-gm.js" ]]; then
            curl -L https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeBASE/download/solidity/v0.6.10-gm.js -o "${STATIC_JS_DIR}"/v0.6.10-gm.js
        fi
    fi
}
get_solc_js

echo "end of script"
exit 0