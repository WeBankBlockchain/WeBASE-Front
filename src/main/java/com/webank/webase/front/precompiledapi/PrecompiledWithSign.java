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
import org.fisco.bcos.web3j.precompile.config.EnumKey;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.fisco.bcos.web3j.precompile.config.SystemConfig.FUNC_SETVALUEBYKEY;

/**
 * send raw transaction through webase-sign to call precompiled
 * @author marsli
 */
@Service
public class PrecompiledWithSign {

	@Autowired
	TransService transService;

	/**
	 * system config: setValueByKey through webase-sign
	 */
	public TransactionReceipt setValueByKey(int groupId, int signUserId, String key, String value)
			throws Exception {
		List<Object> funcParams = new ArrayList<>();
		funcParams.add(key);
		funcParams.add(value);
		// execute set method
		TransactionReceipt response = (TransactionReceipt) transService.transHandleWithSignForPrecompile(groupId, signUserId,
				PrecompiledTypes.SYSTEM_CONFIG, FUNC_SETVALUEBYKEY, funcParams);
		return response;
	}

}
