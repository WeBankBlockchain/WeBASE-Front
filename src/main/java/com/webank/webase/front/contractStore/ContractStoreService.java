/*
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.front.contractStore;

import com.webank.webase.front.contractStore.entity.ContractFolderItem;
import com.webank.webase.front.contractStore.entity.ContractItem;
import com.webank.webase.front.contractStore.entity.StoreItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 *
 */
@Slf4j
@Service
public class ContractStoreService {
    @Autowired
    private ContractItemRepository contractItemRepository;
    @Autowired
    private ContractFolderRepository contractFolderRepository;
    @Autowired
    private ContractStoreRepository contractStoreRepository;

    /**
     *
     */
    public List<StoreItem> getStoreList() {
        List<StoreItem> storeItemList = contractStoreRepository.findAll();
        return storeItemList;
    }

    /**
     *
     */
    public StoreItem getStoreItemById(Long storeId) {
        StoreItem storeItem = contractStoreRepository.findByStoreId(storeId);
        return storeItem;
    }

    /**
     *
     */
    public ContractItem getContractItemById(Long contractId) {
        ContractItem contractItem = contractItemRepository.findByContractId(contractId);
        return contractItem;
    }

    /**
     *
     */
    public List<ContractItem> getContractItemByFolderId(Long contractFolderId) {
        List<ContractItem> contractItemList = contractItemRepository.findByContractFolderId(contractFolderId);
        return contractItemList;
    }

    /**
     *
     */
    public ContractFolderItem getContractFolderById(Long contractFolderId) {
        ContractFolderItem contractFolderItem = contractFolderRepository.findByContractFolderId(contractFolderId);
        return contractFolderItem;
    }

    /**
     *
     */
    public List<ContractFolderItem> getFolderItemListByStoreId(Long storeId) {
        List<ContractFolderItem> contractFolderItemList = contractFolderRepository.findByStoreId(storeId);
        return contractFolderItemList;
    }

    /**
     *
     */
    public List<ContractItem> getContractItemListByFolderId(Long folderId) {
        List<ContractItem> contractItemList = contractItemRepository.findByContractFolderId(folderId);
        return contractItemList;
    }
}
