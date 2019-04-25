#!/usr/bin/env python 
# -*- coding: utf-8 -*-


import requests

from AgentUtils import currentMs
from SinglePointReport import *
from AgentConfig import *

block_info_sessions = []
for i in BROWSER_SERVER_INFO_URLS:
    block_info_sessions.append(requests.Session())

def getBrowserBlockNumber(node_state):
    """get current blockheight of node maneger server"""
    global block_info_sessions
    arguement = {
        "attr": GET_LAST_BLOCK,
        "nodeP2PPort": int(node_state.p2pport),
        "hostIp": HOST_IP

    }

    try:
        rsp = randomPost(block_info_sessions, BROWSER_SERVER_INFO_URLS, arguement)
        rsp_data = json.loads(rsp.text)
        #logger.info("Server rsp: " + str(rsp_data))
        if rsp_data["code"] == 0:
            return rsp_data["data"]["latestBlock"]
        else:
            return None
    except Exception, e:
        logger.error(e)
        info=sys.exc_info()  
        logger.error( "get browser block number except for " + str(info[0]) + "." + str(info[1]))
        return None


def getBlockInfoByNumber(node_state, number):
    arguement = {"jsonrpc":"2.0","method":"eth_getBlockByNumber","params":[str(number), False],"id":3424}
    info = []
    try:
        info = accessNodeRpcPort(arguement, node_state.name, node_state.rpcport)
    except:
        return None
    return info

def get_key(dic, key):
    if dic is None:
        return None
    if dic.has_key(key):
        return dic[key]
    else:
        return None

def blockInfoFilter(info):
    result = info["result"]
    trans_hash_list = list()
    transactions = get_key(result, "transactions")
    if transactions:
        trans_hash_list.append(transactions[0])

    filted_info = {
                    "id":get_key(info, "id"),
                    "jsonrpc":get_key(info, "jsonrpc"),
                    "result":{
                        "gasLimit":get_key(result, "gasLimit"),
                        "gasUsed":get_key(result, "gasUsed"),
                        "hash":get_key(result, "hash"),
                        "miner":get_key(result, "miner"),
                        "number":get_key(result, "number"),
                        "minerNodeId": get_key(result, "minerNodeId"),
                        "timestamp":get_key(result, "timestamp"),
                        "transactions": trans_hash_list
                    }
    }
    return filted_info
              

def uploadBlockInfo(nodes_state):
    logger.info("uploadBlockInfo start.....")
    if not ENABLE_BLOCK_REPORT:
        #logger.info("uploadBlockInfo fail. ENABLE_BLOCK_REPORT:{} ".format(ENABLE_BLOCK_REPORT))
        return
    if len(UPLOAD_BLOCK_NODES) is 0:
        #logger.info("uploadBlockInfo fail. UPLOAD_BLOCK_NODES is None")
        return

    while True:

        for name in UPLOAD_BLOCK_NODES:
            node_state = nodes_state[name]
            time.sleep(float(UPLODA_BLOCK_GAP)/float(len(UPLOAD_BLOCK_NODES)))
            block_number = getBrowserBlockNumber(node_state)
            #logger.info("Browser block number of "+ name + ": " + str(block_number))

            if block_number is not None and block_number is 0:
                block_number = -1

            report_block_gap = 0
            params = []
            if block_number is not None:
                while report_block_gap < REPORT_BLOCK_GAP_LIMIT:
                    block_number += 1
                    info = getBlockInfoByNumber(node_state, block_number)
                    #logger.debug("block info:" + json.dumps(info))
                    if info is None or info["result"] is None:
                        break
                    info = blockInfoFilter(info)
                    #logger.debug("block info:"+json.dumps(info, sort_keys=True, indent=4, separators=(',', ':')))
                    params.append(info)
                    report_block_gap += 1
                postToBrowserServer(BROWSER_SERVER_REPORT_URLS, node_state, BLOCK_INFO, REPORT_REAL_NAME[BLOCK_INFO], params, currentMs())
