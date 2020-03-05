/**
 * Copyright 2014-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.front.precompiledapi;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.transaction.TransService;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.precompile.crud.Condition;
import org.fisco.bcos.web3j.precompile.crud.Entry;
import org.fisco.bcos.web3j.precompile.crud.Table;
import org.fisco.bcos.web3j.precompile.exception.PrecompileMessageException;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.fisco.bcos.web3j.precompile.config.SystemConfig.FUNC_SETVALUEBYKEY;
import static org.fisco.bcos.web3j.precompile.consensus.Consensus.FUNC_ADDOBSERVER;
import static org.fisco.bcos.web3j.precompile.consensus.Consensus.FUNC_ADDSEALER;
import static org.fisco.bcos.web3j.precompile.crud.CRUD.FUNC_UPDATE;
import static org.fisco.bcos.web3j.precompile.crud.TableFactory.FUNC_CREATETABLE;
import static org.fisco.bcos.web3j.precompile.permission.Permission.FUNC_INSERT;
import static org.fisco.bcos.web3j.precompile.permission.Permission.FUNC_REMOVE;

/**
 * send raw transaction through webase-sign to call precompiled
 * @author marsli
 */
@Service
public class PrecompiledWithSignService {

	@Autowired
	TransService transService;
	@Autowired
	Map<Integer, Web3j> web3jMap;

	/**
	 * system config: setValueByKey through webase-sign
	 * @return String result {"code":0,"msg":"success"}
	 */
	public String setValueByKey(int groupId, String fromAddress, String key, String value)
			throws Exception {
		List<Object> funcParams = new ArrayList<>();
		funcParams.add(key);
		funcParams.add(value);
		// execute set method
		TransactionReceipt receipt = (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId, fromAddress,
				PrecompiledTypes.SYSTEM_CONFIG, FUNC_SETVALUEBYKEY, funcParams);
		return PrecompiledCommon.handleTransactionReceipt(receipt, web3jMap.get(groupId));
	}

	/**
	 * permission: grant through webase-sign
	 * @return String result {"code":0,"msg":"success"}
	 */
	public String grant(int groupId, String fromAddress, String tableName, String toAddress) throws Exception {
		List<Object> funcParams = new ArrayList<>();
		funcParams.add(tableName);
		funcParams.add(toAddress);
		TransactionReceipt receipt = (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId, fromAddress,
				PrecompiledTypes.PERMISSION, FUNC_INSERT, funcParams);
		return PrecompiledCommon.handleTransactionReceipt(receipt, web3jMap.get(groupId));
	}

	/**
	 * permission: revoke through webase-sign
	 * @return String result {"code":0,"msg":"success"}
	 */
	public String revoke(int groupId, String fromAddress, String tableName, String toAddress) throws Exception {
		List<Object> funcParams = new ArrayList<>();
		funcParams.add(tableName);
		funcParams.add(toAddress);
		TransactionReceipt receipt = (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId, fromAddress,
				PrecompiledTypes.PERMISSION, FUNC_REMOVE, funcParams);
		return PrecompiledCommon.handleTransactionReceipt(receipt, web3jMap.get(groupId));
	}

	/**
	 * consensus: add sealer through webase-sign
	 */
	public String addSealer(int groupId, String fromAddress, String nodeId) throws Exception {
		// check node id
		if (!isValidNodeID(groupId, nodeId)) {
			return PrecompiledCommon.transferToJson(PrecompiledCommon.P2pNetwork);
		}
		List<String> sealerList = web3jMap.get(groupId).getSealerList().send().getResult();
		if (sealerList.contains(nodeId)) {
			return PrecompiledCommon.transferToJson(PrecompiledCommon.SealerList);
		}
		// trans
		List<Object> funcParams = new ArrayList<>();
		funcParams.add(nodeId);
		TransactionReceipt receipt = (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId, fromAddress,
				PrecompiledTypes.CONSENSUS, FUNC_ADDSEALER, funcParams);
		return PrecompiledCommon.handleTransactionReceipt(receipt, web3jMap.get(groupId));
	}

	/**
	 * consensus: add observer through webase-sign
	 */
	public String addObserver(int groupId, String fromAddress, String nodeId) throws Exception {
		// check node id
		if (!isValidNodeID(groupId, nodeId)) {
			return PrecompiledCommon.transferToJson(PrecompiledCommon.P2pNetwork);
		}
		List<String> observerList = web3jMap.get(groupId).getObserverList().send().getResult();
		if (observerList.contains(nodeId)) {
			return PrecompiledCommon.transferToJson(PrecompiledCommon.ObserverList);
		}
		// trans
		List<Object> funcParams = new ArrayList<>();
		funcParams.add(nodeId);
		TransactionReceipt receipt = (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId, fromAddress,
				PrecompiledTypes.CONSENSUS, FUNC_ADDOBSERVER, funcParams);
		return PrecompiledCommon.handleTransactionReceipt(receipt, web3jMap.get(groupId));
	}

	/**
	 * consensus: remove node from list through webase-sign
	 */
	public String removeNode(int groupId, String fromAddress, String nodeId) throws Exception {
		List<String> groupPeers = web3jMap.get(groupId).getGroupPeers().send().getResult();
		if (!groupPeers.contains(nodeId)) {
			return PrecompiledCommon.transferToJson(PrecompiledCommon.GroupPeers);
		}
		// trans
		List<Object> funcParams = new ArrayList<>();
		funcParams.add(nodeId);
		TransactionReceipt receipt = new TransactionReceipt();
		try {
			receipt = (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId, fromAddress,
					PrecompiledTypes.CONSENSUS, FUNC_REMOVE, funcParams);
		} catch (RuntimeException e) {
			// firstly remove node that sdk connected to the node, return the request that present
			// susscces
			// because the exception is throwed by getTransactionReceipt, we need ignore it.
			if (e.getMessage().contains("Don't send requests to this group")) {
				return PrecompiledCommon.transferToJson(0);
			} else {
				throw e;
			}
		}
		return PrecompiledCommon.handleTransactionReceipt(receipt, web3jMap.get(groupId));
	}

	/**
	 * check node id
	 */
	private boolean isValidNodeID(int groupId, String _nodeID) throws IOException, JsonProcessingException {
		boolean flag = false;
		List<String> nodeIDs = web3jMap.get(groupId).getNodeIDList().send().getResult();
		for (String nodeID : nodeIDs) {
			if (_nodeID.equals(nodeID)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	/**
	 * CRUD: create table through webase-sign
	 */
	public int createTable(int groupId, String fromAddress, Table table) throws Exception {
		List<Object> funcParams = new ArrayList<>();
		funcParams.add(table.getTableName());
		funcParams.add(table.getKey());
		funcParams.add(table.getValueFields());
		TransactionReceipt receipt = (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
				fromAddress, PrecompiledTypes.TABLE_FACTORY, FUNC_CREATETABLE, funcParams);
		return PrecompiledCommon.handleTransactionReceiptForCRUD(receipt);
	}

	/**
	 * CRUD: insert table through webase-sign
	 */
	public int insert(int groupId, String fromAddress, Table table, Entry entry) throws Exception {
		checkTableKeyLength(table);
		// trans
		String entryJsonStr =
				ObjectMapperFactory.getObjectMapper().writeValueAsString(entry.getFields());
		List<Object> funcParams = new ArrayList<>();
		funcParams.add(table.getTableName());
		funcParams.add(table.getKey());
		funcParams.add(entryJsonStr);
		funcParams.add(table.getOptional());
		TransactionReceipt receipt = (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
				fromAddress, PrecompiledTypes.CRUD, FUNC_INSERT, funcParams);
		return PrecompiledCommon.handleTransactionReceiptForCRUD(receipt);
	}

	/**
	 * CRUD: update table through webase-sign
	 */
	public int update(int groupId, String fromAddress, Table table,
					  Entry entry, Condition condition) throws Exception {
		checkTableKeyLength(table);
		// trans
		String entryJsonStr =
				ObjectMapperFactory.getObjectMapper().writeValueAsString(entry.getFields());
		String conditionStr =
				ObjectMapperFactory.getObjectMapper().writeValueAsString(condition.getConditions());
		List<Object> funcParams = new ArrayList<>();
		funcParams.add(table.getTableName());
		funcParams.add(table.getKey());
		funcParams.add(entryJsonStr);
		funcParams.add(conditionStr);
		funcParams.add(table.getOptional());
		TransactionReceipt receipt = (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
				fromAddress, PrecompiledTypes.CRUD, FUNC_UPDATE, funcParams);
		return PrecompiledCommon.handleTransactionReceiptForCRUD(receipt);
	}

	/**
	 * CRUD: remove table through webase-sign
	 */
	public int remove(int groupId, String fromAddress, Table table, Condition condition) throws Exception {
		checkTableKeyLength(table);
		// trans
		String conditionStr =
				ObjectMapperFactory.getObjectMapper().writeValueAsString(condition.getConditions());
		List<Object> funcParams = new ArrayList<>();
		funcParams.add(table.getTableName());
		funcParams.add(table.getKey());
		funcParams.add(conditionStr);
		funcParams.add(table.getOptional());
		TransactionReceipt receipt = (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId,
				fromAddress, PrecompiledTypes.CRUD, FUNC_REMOVE, funcParams);
		return PrecompiledCommon.handleTransactionReceiptForCRUD(receipt);
	}

	private void checkTableKeyLength(Table table) throws PrecompileMessageException {
		if (table.getKey().length() > PrecompiledCommon.TABLE_KEY_MAX_LENGTH) {
			throw new PrecompileMessageException(
					"The value of the table key exceeds the maximum limit("
							+ PrecompiledCommon.TABLE_KEY_MAX_LENGTH
							+ ").");
		}
	}
}
