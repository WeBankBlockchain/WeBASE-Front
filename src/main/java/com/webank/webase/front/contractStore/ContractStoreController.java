/**
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

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.controller.BaseController;
import com.webank.webase.front.base.response.BaseResponse;
import com.webank.webase.front.contractStore.entity.ContractFolderItem;
import com.webank.webase.front.contractStore.entity.ContractItem;
import com.webank.webase.front.contractStore.entity.StoreItem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * ContractStoreController.
 */
@Api(value = "/contractStore", tags = "contractStore interface")
@Slf4j
@RestController
@RequestMapping(value = "/contractStore")
public class ContractStoreController extends BaseController {

    @Autowired
    ContractStoreService contractStoreService;


    /**
     * query the list of contract store item
     */
    @ApiOperation(value = "the list of contract store item", notes = "get contractStoreList")
    @GetMapping(value = "/getContractStoreList")
    public BaseResponse getContractStoreList() {

        List<StoreItem>  storeItemList = contractStoreService.getStoreList();
        BaseResponse response = new BaseResponse(ConstantCode.RET_SUCCEED);
        response.setData(storeItemList);
        return response;
    }

    /**
     * query a contract store item
     */
    @ApiOperation(value = "get contractStoreItem", notes = "get contractStoreItem")
    @ApiImplicitParam(name = "storeId", value = "storeId", required = true,
            dataType = "int")
    @GetMapping(value = "/getContractStoreById/{storeId}")
    public BaseResponse getContractStoreById(@PathVariable("storeId") Integer storeId) {
        log.info("getContractStoreById start. storeId:{}", storeId);
        StoreItem storeItem = contractStoreService.getStoreItemById(storeId.longValue());
        BaseResponse response = new BaseResponse(ConstantCode.RET_SUCCEED);
        response.setData(storeItem);
        return response;
    }

    /**
     * query a contract folder item
     */
    @ApiOperation(value = "get contractFolderItem", notes = "get contractFolderItem")
    @ApiImplicitParam(name = "contractFolderId", value = "contractFolderId", required = true,
            dataType = "int")
    @GetMapping(value = "/getContractFolderById/{contractFolderId}")
    public BaseResponse getContractFolderById(@PathVariable("contractFolderId") Integer contractFolderId) {
        log.info("getContractStoreById start. contractFolderId:{}", contractFolderId);
        ContractFolderItem contractFolderItem = contractStoreService.getContractFolderById(contractFolderId.longValue());
        BaseResponse response = new BaseResponse(ConstantCode.RET_SUCCEED);
        response.setData(contractFolderItem);
        return response;
    }

    /**
     * query a contract item
     */
    @ApiOperation(value = "get a contractItem", notes = "get a contractItem")
    @ApiImplicitParam(name = "contractId", value = "contractId", required = true,
            dataType = "int")
    @GetMapping(value = "/getContractItemById/{contractId}")
    public BaseResponse getContractItemById(@PathVariable("contractId") Integer contractId) {
        log.info("getContractStoreById start. contractId:{}", contractId);
        ContractItem contractItem = contractStoreService.getContractItemById(contractId.longValue());
        BaseResponse response = new BaseResponse(ConstantCode.RET_SUCCEED);
        response.setData(contractItem);
        return response;
    }

    /**
     * get folderItemList by storeId
     */
    @ApiOperation(value = "get folderItemList by storeId", notes = "get folderItemList by storeId")
    @ApiImplicitParam(name = "storeId", value = "storeId", required = true,
            dataType = "int")
    @GetMapping(value = "/getFolderItemListByStoreId/{storeId}")
    public BaseResponse getFolderItemListByStoreId(@PathVariable("storeId") Integer storeId) {
        log.info("getFolderItemListByStoreId start. storeId:{}", storeId);
        List<ContractFolderItem> contractFolderItemList = contractStoreService.getFolderItemListByStoreId(storeId.longValue());
        BaseResponse response = new BaseResponse(ConstantCode.RET_SUCCEED);
        response.setData(contractFolderItemList);
        return response;
    }

    /**
     * get contractItemList by folderId
     */
    @ApiOperation(value = "get contractItemList by folderId", notes = "get contractItemList by folderId")
    @ApiImplicitParam(name = "folderId", value = "folderId", required = true,
            dataType = "int")
    @GetMapping(value = "/getContractItemByFolderId/{folderId}")
    public BaseResponse getContractItemByFolderId(@PathVariable("folderId") Integer folderId) {
        log.info("getContractItemByFolderId start. storeId:{}", folderId);
        List<ContractItem> contractItemList = contractStoreService.getContractItemListByFolderId(folderId.longValue());
        BaseResponse response = new BaseResponse(ConstantCode.RET_SUCCEED);
        response.setData(contractItemList);
        return response;
    }
}
