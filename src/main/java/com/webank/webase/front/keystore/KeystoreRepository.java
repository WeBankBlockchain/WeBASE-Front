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
package com.webank.webase.front.keystore;


import com.webank.webase.front.keystore.entity.KeyStoreInfo;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface KeystoreRepository extends CrudRepository<KeyStoreInfo, String>,
    JpaSpecificationExecutor<KeyStoreInfo> {

    KeyStoreInfo findByAddress(String address);

    KeyStoreInfo findBySignUserId(String signUserId);

    KeyStoreInfo findByUserName(String userName);

    KeyStoreInfo findByUserNameAndType(String userName, int type);
}
