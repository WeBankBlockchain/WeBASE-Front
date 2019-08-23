/**
 * Copyright 2014-2019  the original author or authors.
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
import com.webank.webase.front.method.entity.Method;
import com.webank.webase.front.method.entity.NewMethodInputParam;
import com.webank.webase.front.method.entity.TbMethod;
import com.webank.webase.front.method.entity.TbMethodKey;

@Service
public class MethodService {

    @Autowired
    private MethodRepository methodRepository;

    /**
     * save method info.
     */
    public void saveMethod(NewMethodInputParam newMethodInputParam) {
        List<Method> methodList = newMethodInputParam.getMethodList();
        TbMethod tbMethod = new TbMethod();
        tbMethod.setGroupId(newMethodInputParam.getGroupId());
        
        //save each method
        for (Method method : methodList) {
            BeanUtils.copyProperties(method, tbMethod);
            methodRepository.save(tbMethod);
        }
    }

    /**
     * query by methodId.
     */
    public TbMethod getByMethodId(String methodId, int groupId) {
        TbMethodKey pram = new TbMethodKey();
        pram.setMethodId(methodId);
        pram.setGroupId(groupId);
        return methodRepository.findOne(pram);
    }
}
