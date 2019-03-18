#!/usr/bin/env python 
# -*- coding: utf-8 -*-

from AgentNetwork import *


def accessRpcAndPost(node_state, method, params, handle_rpc_result):
    arguement = {"jsonrpc": "2.0", "method": method, "params": params, "id": 3424}

    result = []
    try:
        result = accessNodeRpcPort(arguement, node_state.name, node_state.rpcport)
    except Exception as ex:
        logger.info("RPC port not avaliable. error:{}".format(ex))
        return None
    msg = handle_rpc_result(result)
    return msg
