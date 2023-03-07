/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.contractStore;

import com.webank.webase.front.base.SpringTestBase;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.contractStore.entity.ContractFolderItem;
import com.webank.webase.front.contractStore.entity.ContractItem;
import com.webank.webase.front.contractStore.entity.StoreItem;
import com.webank.webase.front.util.JsonUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

public class ContractStoreServiceTest extends SpringTestBase {

    @Autowired
    ContractStoreService contractStoreService;
    @Autowired
    ContractItemRepository contractItemRepository;

    @Test
    public void testStoreList() {
        List<StoreItem> storeItems = contractStoreService.getStoreList();
        for (StoreItem storeItem : storeItems) {
            storeItem.setCreateTime(null);
            storeItem.setModifyTime(null);
            System.out.println(JsonUtils.objToString(storeItem));
        }

        for (StoreItem item : storeItems) {
            List<ContractFolderItem> folderList = contractStoreService.getFolderItemListByStoreId(item.getStoreId());
            for (ContractFolderItem folderItem : folderList) {
                folderItem.setCreateTime(null);
                folderItem.setModifyTime(null);
                System.out.println(JsonUtils.objToString(folderItem));
            }
        }

        for (StoreItem item : storeItems) {
            List<ContractFolderItem> folderList = contractStoreService.getFolderItemListByStoreId(item.getStoreId());
            for (ContractFolderItem folderItem : folderList) {
                List<ContractItem> contractItemList = contractStoreService.getContractItemListByFolderId(folderItem.getContractFolderId());
                for (ContractItem contractItem : contractItemList) {
                    contractItem.setCreateTime(null);
                    contractItem.setModifyTime(null);
                    System.out.println(JsonUtils.objToString(contractItem));
                }

            }
        }
    }

    @Test
    public void testPrintAll() {

        List<ContractItem> contractItemList = contractItemRepository.findAll();
        for (ContractItem contractItem : contractItemList) {
            contractItem.setCreateTime(null);
            contractItem.setModifyTime(null);
            System.out.println(JsonUtils.objToString(contractItem));
        }
    }


}
