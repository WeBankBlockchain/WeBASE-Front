/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.front.configapi;

import com.webank.webase.front.configapi.entity.ConfigInfo;
import com.webank.webase.front.contract.entity.Contract;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface ConfigInfoRepository extends CrudRepository<ConfigInfo, Long>,
    JpaSpecificationExecutor<ConfigInfo> {

    List<ConfigInfo> findByType(String type);
    ConfigInfo findByTypeAndKey(String type, String key);
}
