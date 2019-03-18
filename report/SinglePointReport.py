#!/usr/bin/env python 
# -*- coding: utf-8 -*-

import ReportWorker
from AgentNetwork import *
from AgentUtils import currentMs
from AgentConfig import *

############# RPC port info report #################


def handleRpcResult1(msg):
    return int(msg["result"], 16)


def handleRpcResult2(msg):
    params = []
    params.append(msg)
    return params


def handleRpcResult3(msg):
    return len(msg["result"]["transactions"])


def accessBlockNumber(node_state):
    """get node's block height"""
    block_number = ReportWorker.accessRpcAndPost(node_state, "eth_blockNumber", [], handleRpcResult1)
    postToBrowserServer(BROWSER_SERVER_REPORT_URLS, node_state, BLOCK_HEIGHT, REPORT_REAL_NAME[BLOCK_HEIGHT], block_number, currentMs())


def accessBlockView(node_state):
    pbft_view = ReportWorker.accessRpcAndPost(node_state, "eth_pbftView", [], handleRpcResult1)
    postToBrowserServer(BROWSER_SERVER_REPORT_URLS, node_state, PBFT_VIEW, REPORT_REAL_NAME[PBFT_VIEW], pbft_view, currentMs())


def accessPendingTransactions(node_state):
    """get pending transactiong"""
    pending_transactions = ReportWorker.accessRpcAndPost(node_state, "eth_pendingTransactions", [], handleRpcResult2)
    #logger.info("pending_transactions:"+json.dumps(pending_transactions))
    pending_trans_count = 0  # Number of transactions being processed
    if pending_transactions:
        # traverse metricValue
        for pending_obj in pending_transactions:
            if pending_obj is not None:
                result_json = pending_obj["result"]
                
                pending_trans_count += len(result_json["current"])
                pending_trans_count += len(result_json["pending"])
        postToBrowserServer(BROWSER_SERVER_REPORT_URLS, node_state, PENDING_TX_COUNT, REPORT_REAL_NAME[PENDING_TX_COUNT], pending_trans_count, currentMs())


def accessRpc(node_state):
    """access Rpc port to get node info"""
    if ENABLE_SINGLE_POINT_REPORT:
        accessBlockNumber(node_state)
        accessBlockView(node_state)
        # accessPendingTransactions(node_state)