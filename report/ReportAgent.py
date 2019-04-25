#!/usr/bin/env python 
# -*- coding: utf-8 -*-

import threading
import time

import BlockReport
import SinglePointReport
import ReportLogs
from AgentNetwork import *
from BlockReport import uploadBlockInfo
from ReportWorker import postWorker

nodes_state = dict()

class NodeState(object):
    def __init__(self, node):
        self.name = node["name"]
        self.rpcport = node["rpcport"]
        self.p2pport = node["p2pport"]
        self.chainname = node["chainname"] 

        if not node.has_key("subsystemid"):
            self.subsystemid = -1
        else:
            self.subsystemid = node["subsystemid"]
       # self.last_alert = dict()
        self.filter_time = 0
        self.flow_filter_time = 0
        self.tx_flow_log = []
        self.block_flow_log = []
        self.lock = threading.Lock()

        self.log_time_format = '%Y-%m-%d %H:%M:%S'
        self.log_filename_format = "%Y%M%d%H"
        self.log_dir = node["log_dir"]

    pass


def accessNodeByTime(interval, node):
    """query and report every interval"""
    while True:
        #logger.info( "accessNodeInfo " + node["name"] + " " + str(node["rpcport"]) + " " + str(node["p2pport"]) + " " + str(node["logconf"]))
        SinglePointReport.accessRpc(nodes_state[node["name"]])
        time.sleep(interval)

def callBlockReporter():
    if ENABLE_BLOCK_REPORT:
        BlockReport.uploadBlockInfo(nodes_state)


def callReportLogs():
    if ENABLE_ERROR_LOG_REPORT:
        ReportLogs.uploadLogsInfo(nodes_state)


def main():
    try:
        for node in BLOCKCHAIN_NODE_CONFIGS:
            nodes_state[node["name"]] = NodeState(node)  # init node state

        # An Agent thread of each node
        for node in BLOCKCHAIN_NODE_CONFIGS:
            t = threading.Thread(target=accessNodeByTime, args=(ACCESS_NODE_INTERVAL, node), name="thread_access_node")
            t.start()

        # thread report block info
        upload_block_thread = threading.Thread(target=callBlockReporter, name="callBlockReporter")
        upload_block_thread.start()


        # report logs
        upload_logs_thread = threading.Thread(target=callReportLogs, name="callReportLogs")
        upload_logs_thread.start()

        # every queue has a thread
        for i in range(0, POST_WORKER_NUM):
            post_worker = threading.Thread(target=postWorker, args=([i]), name="postWorker")
            post_worker.start()
    except Exception, e:
        logger.error("ERROR：{}".format(e))

if __name__ == "__main__":
    main()
