/**
 * Copyright 2014-2020  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.front.method;

import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.webank.webase.front.method.entity.MethodHandle;
import com.webank.webase.front.method.entity.NewMethodInputParamHandle;
import com.webank.webase.front.method.entity.Method;
import com.webank.webase.front.method.entity.MethodKey;

/**
 * Method is used in transaction data's analysis
 * Store contract's method in db
 */
@Service
public class MethodService {

    @Autowired
    private MethodRepository methodRepository;

    /**
     * save method info.
     */
    public void saveMethod(NewMethodInputParamHandle newMethodInputParamHandle) {
        List<MethodHandle> methodHandleList = newMethodInputParamHandle.getMethodHandleList();
        Method method = new Method();
        method.setGroupId(newMethodInputParamHandle.getGroupId());
        
        //save each method
        for (MethodHandle methodHandle : methodHandleList) {
            BeanUtils.copyProperties(methodHandle, method);
            methodRepository.save(method);
        }
    }

    /**
     * query by methodId.
     */
    public Method getByMethodId(String methodId, int groupId) {
        MethodKey pram = new MethodKey();
        pram.setMethodId(methodId);
        pram.setGroupId(groupId);
        return methodRepository.findById(pram).orElse(null);
    }
}
