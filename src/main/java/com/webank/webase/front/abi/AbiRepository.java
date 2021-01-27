/**
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.front.abi;

import com.webank.webase.front.abi.entity.AbiInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author marsli
 */
public interface AbiRepository extends CrudRepository<AbiInfo, Long>,
        JpaSpecificationExecutor<AbiInfo> {

    AbiInfo findByAbiId(Long abiId);

    AbiInfo findByGroupIdAndContractAddress(Integer groupId, String address);

    AbiInfo findByGroupIdAndContractName(Integer groupId, String userName);

    List<AbiInfo> findByGroupId(Integer groupId, Pageable pageable);

    List<AbiInfo> findByGroupId(Integer groupId);
}
