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


import com.webank.webase.front.base.enums.PrecompiledTypes;
import com.webank.webase.front.transaction.TransService;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.fisco.bcos.web3j.precompile.config.SystemConfig.FUNC_SETVALUEBYKEY;
import static org.fisco.bcos.web3j.precompile.permission.Permission.FUNC_INSERT;
import static org.fisco.bcos.web3j.precompile.permission.Permission.FUNC_REMOVE;

/**
 * send raw transaction through webase-sign to call precompiled
 * @author marsli
 */
@Service
public class PrecompiledWithSign {

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
}
