package com.webank.webase.front.contractStore;

import com.webank.webase.front.contractStore.constant.EvidenceConstantContext;
import com.webank.webase.front.contractStore.constant.PointsConstantContext;
import com.webank.webase.front.contractStore.constant.ProxyConstantContext;
import com.webank.webase.front.contractStore.constant.SmartDevConstantContext;
import com.webank.webase.front.contractStore.constant.ToolsConstantContext;
import com.webank.webase.front.contractStore.entity.ContractFolderItem;
import com.webank.webase.front.contractStore.entity.ContractItem;
import com.webank.webase.front.contractStore.entity.StoreItem;
import com.webank.webase.front.util.JsonUtils;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PresetDataService {

    @Autowired
    private ContractItemRepository contractItemRepository;
    @Autowired
    private ContractFolderRepository contractFolderRepository;
    @Autowired
    private ContractStoreRepository contractStoreRepository;

    public static final Integer toolboxId = 1;
    public static final Integer evidenceId = 2;
    public static final Integer pointsId = 3;
    public static final Integer smartDevId = 4;

    public static final Integer smartDevToolFolderId = 4;
    public static final Integer smartDevEvidenceFolderId = 5;
    public static final Integer assetFolderId = 6;
    public static final Integer traceFolderId = 7;
    public static final Integer proxyFolderId = 8;


    public void initStoreItem() {
        insertStoreItem(toolboxId, "工具箱", "Toolbox", "1", "toolboxId",
            "工具箱中有常用的工具合约", "工具箱中有常用的工具合约",
            "Toolbox Contract suite", "Toolbox Contract suite");
        insertStoreItem(evidenceId, "存证应用", "Evidence", "2", "evidenceId",
            "一套区块链存证合约，实现区块链存证、取证",
            "一套区块链存证合约，实现区块链存证、取证",
            "Evidence Contract suite", "Evidence Contract suite");
        insertStoreItem(pointsId, "积分应用", "Points", "3", "pointsId",
            "一套积分合约，具有积分相关的增发，销毁，暂停合约，黑白名单等权限控制等功能",
            "一套积分合约，具有积分相关的增发，销毁，暂停合约，黑白名单等权限控制等功能",
            "Points Contract suite", "Points Contract suite");
        insertStoreItem(smartDevId, "SmartDev存证应用", "Smart_Dev_Evidence", "4", "evidenceId",
            "Smart-Dev-Contract仓库中的存证应用模板",
            "Smart-Dev-Contract\'s Evidence Contract suite of business_template",
            "Smart-Dev-Contracts仓库中的存证应用模板",
                    "Smart-Dev-Contract\'s Evidence Contract suite of business_template");
        insertStoreItem(assetFolderId, "资产应用", "Asset", "4", "pointsId",
            "一套非同质化资产合约，具有于唯一性资产类型，如房产、汽车、道具、版权等",
            "Asset Contract Suite",
            "一套非同质化资产合约，具有于唯一性资产类型，如房产、汽车、道具、版权等",
                    "Asset Contract Suite");
        insertStoreItem(traceFolderId, "溯源应用", "Traceability", "4", "traceId",
            "一套溯源应用合约模板（Smart-Dev-Contract）",
            "Traceability Contract Suite",
            "一套溯源应用合约模板（Smart-Dev-Contract）",
            "Traceability Contract Suite");
        insertStoreItem(proxyFolderId, "代理合约模板", "Proxy", "4", "toolboxId",
            "一套可升级的业务、数据分离的代理合约模板",
            "Proxy Contract Suite",
            "一套可升级的业务、数据分离的代理合约模板",
            "Proxy Contract Suite");

    }

    public void insertStoreItem(long storeId, String storeName, String StoreName_en, String StoreType,
                                String StoreIcon, String StoreDesc, String StoreDetail,String StoreDesc_en,
                                String StoreDetail_en)
    {
        log.info("insert storeItem id:{},storeName:{}", storeId, storeName);
        if (contractStoreRepository.existsById(storeId)) {
            log.info("insertStoreItem skip storeId:{} already exist", storeId);
            return;
        }
        StoreItem storeItem = new StoreItem();
        storeItem.setStoreId(storeId);
        storeItem.setStoreName(storeName);
        storeItem.setStoreName_en(StoreName_en);
        storeItem.setStoreType(StoreType);
        storeItem.setStoreIcon(StoreIcon);
        storeItem.setStoreDesc(StoreDesc);
        storeItem.setStoreDetail(StoreDetail);
        storeItem.setStoreDesc_en(StoreDesc_en);
        storeItem.setStoreDetail_en(StoreDetail_en);
        storeItem.setCreateTime(LocalDateTime.now());
        storeItem.setModifyTime(storeItem.getCreateTime());
        contractStoreRepository.save(storeItem);
    }

    public void initContractFolderItem() {
        int contractFolderIndex = 1;
        insertContractFolderItem(toolboxId,toolboxId,"Tools",
                "工具箱中有常用的工具合约",
                "工具箱中有常用的工具合约",
                "Toolbox Contract Suite",
                "Toolbox Contract Suite");

        insertContractFolderItem(evidenceId,evidenceId,"Evidence",
                "一套区块链存证合约，实现区块链存证、取证",
                "一套区块链存证合约，实现区块链存证、取证",
                "Evidence Contract Suite",
                "Evidence Contract Suite");

        insertContractFolderItem(pointsId,pointsId,"Points",
                "一套积分合约，具有积分相关的增发，销毁，暂停合约，黑白名单等权限控制等功能",
                "一套积分合约，具有积分相关的增发，销毁，暂停合约，黑白名单等权限控制等功能",
                "Points Contract Suite",
                "Points Contract Suite");
        // belong to points store, and new folder
        insertContractFolderItem(smartDevToolFolderId,toolboxId,"Smart_Dev_Basic",
            "SmartDev基础合约，包含Table/KVTable/Sha/Crypto/HelloWorld等",
            "SmartDev基础合约，包含Table/KVTable/Sha/Crypto/HelloWorld等",
            "Smart-Dev-Contract basic contract suite, including Table/KVTable/Sha/Crypto/HelloWorld etc.",
            "Smart-Dev-Contract basic contract suite, including Table/KVTable/Sha/Crypto/HelloWorld etc.");

        insertContractFolderItem(smartDevEvidenceFolderId,smartDevId,"Smart_Dev_Evidence",
            "SmartDev存证合约案例",
            "SmartDev存证合约案例",
            "Smart-Dev-Contract Evidence contract suite",
            "Smart-Dev-Contract Evidence contract suite");
        insertContractFolderItem(assetFolderId,assetFolderId,"Asset",
            "一套非同质化资产合约，具有于唯一性资产类型，如房产、汽车、道具、版权等，具有增发、销毁，暂停合约，黑白名单等权限控制等功能",
            "一套非同质化资产合约，具有于唯一性资产类型，如房产、汽车、道具、版权等，具有增发、销毁，暂停合约，黑白名单等权限控制等功能",
            "Asset Contract Suite",
            "Asset Contract Suite");
        insertContractFolderItem(traceFolderId,traceFolderId,"Traceability",
            "一套溯源应用合约模板（Smart-Dev-Contract）",
            "一套溯源应用合约模板（Smart-Dev-Contract）",
            "Traceability Contract Suite",
            "Traceability Contract Suite");
        insertContractFolderItem(proxyFolderId,proxyFolderId,"Proxy",
                "一套可升级的业务、数据分离的代理合约模板",
                "一套可升级的业务、数据分离的代理合约模板",
                "Proxy Contract Suite",
                "Proxy Contract Suite");
    }

    public void insertContractFolderItem(long contractFolderId, long storeId, String contractFolderName, String contractFolderDesc, String contractFolderDetail,
                                         String contractFolderDesc_en, String contractFolderDetail_en)
    {
        log.info("insert contractFolderItem id:{},contractFolderName:{} ", contractFolderId, contractFolderName);
        if (contractFolderRepository.existsById(contractFolderId)) {
            log.info("insertContractFolderItem skip contractFolderId:{}  already exist", contractFolderId);
            return;
        }
        ContractFolderItem contractFolderItem = new ContractFolderItem();
        contractFolderItem.setContractFolderId(contractFolderId);
        contractFolderItem.setStoreId(storeId);
        contractFolderItem.setContractFolderName(contractFolderName);
        contractFolderItem.setContractFolderDesc(contractFolderDesc);
        contractFolderItem.setContractFolderDetail(contractFolderDetail);
        contractFolderItem.setContractFolderDesc_en(contractFolderDesc_en);
        contractFolderItem.setContractFolderDetail_en(contractFolderDetail_en);
        contractFolderItem.setCreateTime(LocalDateTime.now());
        contractFolderItem.setModifyTime(contractFolderItem.getCreateTime());
        contractFolderRepository.save(contractFolderItem);
    }

    public void initContractItem() {
        int contractIndex = 1;
        //tools
        insertContractItem(contractIndex++,toolboxId,"Address", ToolsConstantContext.ADDRESS_SOURCE,
                ToolsConstantContext.ADDRESS_MD,ToolsConstantContext.ADDRESS_MD);
        insertContractItem(contractIndex++,toolboxId,"LibString",ToolsConstantContext.LIB_STRING_SOURCE,
                ToolsConstantContext.LIB_STRING_MD,ToolsConstantContext.LIB_STRING_MD);
        insertContractItem(contractIndex++,toolboxId,"SafeMath",ToolsConstantContext.SAFE_MATH_SOURCE,
                ToolsConstantContext.SAFE_MATH_MD,ToolsConstantContext.SAFE_MATH_MD);
        insertContractItem(contractIndex++,toolboxId,"Table",ToolsConstantContext.TABLE_SOURCE,
                ToolsConstantContext.TABLE_MD,ToolsConstantContext.TABLE_MD);
        insertContractItem(contractIndex++,toolboxId,"Roles",ToolsConstantContext.ROLES_SOURCE,
                ToolsConstantContext.ROLES_MD,ToolsConstantContext.ROLES_MD);

        //evidence
        insertContractItem(contractIndex++,evidenceId,"Evidence",
                EvidenceConstantContext.EVIDENCE_SOURCE,
                EvidenceConstantContext.EVIDENCE_MD,"");
        insertContractItem(contractIndex++,evidenceId,"EvidenceSignersData",
                EvidenceConstantContext.EVIDENCE_SIGNERS_DATA_SOURCE,
                EvidenceConstantContext.EVIDENCE_MD,"");


        //Points
        insertContractItem(contractIndex++,pointsId,"BAC001",
                PointsConstantContext.BAC001_SOURCE,
                PointsConstantContext.BAC001_MD,"");
        insertContractItem(contractIndex++,pointsId,"IBAC001",
                PointsConstantContext.I_BAC001_SOURCE,
                PointsConstantContext.BAC001_MD,"");
        insertContractItem(contractIndex++,pointsId,"Roles",ToolsConstantContext.ROLES_SOURCE,
                ToolsConstantContext.ROLES_MD,"");
        insertContractItem(contractIndex++,pointsId,"SafeMath",ToolsConstantContext.SAFE_MATH_SOURCE,
                ToolsConstantContext.SAFE_MATH_MD,"");
        insertContractItem(contractIndex++,pointsId,"Address",ToolsConstantContext.ADDRESS_SOURCE,
                ToolsConstantContext.ADDRESS_MD,"");

        // smart dev
        // tools
        insertContractItem(contractIndex++,smartDevToolFolderId,"Crypto",ToolsConstantContext.CRYPTO_SOURCE,
            ToolsConstantContext.CRYPTO_MD,ToolsConstantContext.CRYPTO_MD);
        insertContractItem(contractIndex++,smartDevToolFolderId,"ShaTest",ToolsConstantContext.CRYPTO_SHA_SOURCE,
            ToolsConstantContext.CRYPTO_SHA_TEST_MD,ToolsConstantContext.CRYPTO_SHA_TEST_MD);
        insertContractItem(contractIndex++,smartDevToolFolderId,"HelloWorld",ToolsConstantContext.HELLO_WORLD_SOURCE,
            ToolsConstantContext.HELLO_WORLD_MD,ToolsConstantContext.HELLO_WORLD_MD);
        insertContractItem(contractIndex++,smartDevToolFolderId,"KVTableTest",ToolsConstantContext.KV_TABLE_TEST_SOURCE,
            ToolsConstantContext.KVTABLE_TEST_MD,ToolsConstantContext.KVTABLE_TEST_MD);
        insertContractItem(contractIndex++,smartDevToolFolderId,"Table",ToolsConstantContext.TABLE_SOURCE,
            ToolsConstantContext.TABLE_CRUD_MD,ToolsConstantContext.TABLE_CRUD_MD);
        insertContractItem(contractIndex++,smartDevToolFolderId,"TableTest",ToolsConstantContext.TABLE_TEST_SOURCE,
            ToolsConstantContext.TABLE_CRUD_TEST_MD,ToolsConstantContext.TABLE_CRUD_TEST_MD);
        // evidence
        insertContractItem(contractIndex++,smartDevEvidenceFolderId,"EvidenceController", SmartDevConstantContext.EVI_CONTROLLER_SOURCE,
            SmartDevConstantContext.EVIDENCE_API_MD,SmartDevConstantContext.EVIDENCE_API_MD);
        insertContractItem(contractIndex++,smartDevEvidenceFolderId,"EvidenceRepository",SmartDevConstantContext.EVI_REPO_SOURCE,
            SmartDevConstantContext.EVIDENCE_API_MD,SmartDevConstantContext.EVIDENCE_API_MD);
        insertContractItem(contractIndex++,smartDevEvidenceFolderId,"RequestRepository",SmartDevConstantContext.REQUEST_REPO_SOURCE,
            SmartDevConstantContext.EVIDENCE_API_MD,SmartDevConstantContext.EVIDENCE_API_MD);
        insertContractItem(contractIndex++,smartDevEvidenceFolderId,"Authentication",SmartDevConstantContext.AUTH_SOURCE,
            SmartDevConstantContext.EVIDENCE_API_MD,SmartDevConstantContext.EVIDENCE_API_MD);

        // asset
        insertContractItem(contractIndex++,assetFolderId,"BAC002",
            PointsConstantContext.BAC002_SOURCE, PointsConstantContext.BAC002_MD,"");
        insertContractItem(contractIndex++,assetFolderId,"IBAC002",
            PointsConstantContext.I_BAC002_SOURCE, PointsConstantContext.BAC002_MD,"");
        // asset tool
        insertContractItem(contractIndex++,assetFolderId,"Counters",ToolsConstantContext.COUNTERS_SOURCE,
            ToolsConstantContext.ROLES_MD,ToolsConstantContext.COUNTERS_MD);
        insertContractItem(contractIndex++,assetFolderId,"Register",ToolsConstantContext.REGISTER_SOURCE,
            ToolsConstantContext.ROLES_MD,ToolsConstantContext.REGISTER_MD);
        insertContractItem(contractIndex++,assetFolderId,"Roles",ToolsConstantContext.ROLES_SOURCE,
            ToolsConstantContext.ROLES_MD,ToolsConstantContext.ROLES_MD);
        insertContractItem(contractIndex++,assetFolderId,"SafeMath",ToolsConstantContext.SAFE_MATH_SOURCE,
            ToolsConstantContext.SAFE_MATH_MD,ToolsConstantContext.ROLES_MD);

        // v1.5.2 tools
        insertContractItem(contractIndex++,toolboxId,"Counters",ToolsConstantContext.COUNTERS_SOURCE,
            ToolsConstantContext.ROLES_MD,ToolsConstantContext.COUNTERS_MD);
        insertContractItem(contractIndex++,toolboxId,"Register",ToolsConstantContext.REGISTER_SOURCE,
            ToolsConstantContext.ROLES_MD,ToolsConstantContext.REGISTER_MD);
        // 1.5.3
        // traceability
        insertContractItem(contractIndex++,traceFolderId,"Goods",SmartDevConstantContext.GOODS,
            SmartDevConstantContext.TRACE_API_MD,SmartDevConstantContext.TRACE_API_MD);
        insertContractItem(contractIndex++,traceFolderId,"Traceability",SmartDevConstantContext.TRACEABILITY,
            SmartDevConstantContext.TRACE_API_MD,SmartDevConstantContext.TRACE_API_MD);
        insertContractItem(contractIndex++,traceFolderId,"TraceabilityFactory",SmartDevConstantContext.TRACEABILITY_FACTORY,
            SmartDevConstantContext.TRACE_API_MD,SmartDevConstantContext.TRACE_API_MD);
        // proxy
        insertContractItem(contractIndex++,proxyFolderId,"EnrollProxy", ProxyConstantContext.ENROLL_PROXY_SOURCE,
                ProxyConstantContext.PROXY_MD, ProxyConstantContext.PROXY_MD);
        insertContractItem(contractIndex++,proxyFolderId,"EnrollController", ProxyConstantContext.ENROLL_CONTROLLER_SOURCE,
                ProxyConstantContext.PROXY_MD, ProxyConstantContext.PROXY_MD);
        insertContractItem(contractIndex++,proxyFolderId,"EnrollStorage", ProxyConstantContext.ENROLL_STORAGE_SOURCE,
                ProxyConstantContext.PROXY_MD, ProxyConstantContext.PROXY_MD);
        insertContractItem(contractIndex++,proxyFolderId,"EnrollStorageStateful", ProxyConstantContext.ENROLL_STORAGE_STATEFUL_SOURCE,
                ProxyConstantContext.PROXY_MD, ProxyConstantContext.PROXY_MD);
        insertContractItem(contractIndex++,proxyFolderId,"Proxy", ProxyConstantContext.PROXY_SOURCE,
                ProxyConstantContext.PROXY_MD, ProxyConstantContext.PROXY_MD);
        insertContractItem(contractIndex++,proxyFolderId,"BaseStorage", ProxyConstantContext.BASE_STORAGE_SOURCE,
                ProxyConstantContext.PROXY_MD, ProxyConstantContext.PROXY_MD);
        insertContractItem(contractIndex++,proxyFolderId,"Ownable", ProxyConstantContext.OWNABLE_SOURCE,
                ProxyConstantContext.PROXY_MD, ProxyConstantContext.PROXY_MD);
        insertContractItem(contractIndex++,proxyFolderId,"Strings", ProxyConstantContext.STRINGS_SOURCE,
                ProxyConstantContext.PROXY_MD, ProxyConstantContext.PROXY_MD);
        insertContractItem(contractIndex++,proxyFolderId,"Table", ProxyConstantContext.TABLE_SOURCE,
                ProxyConstantContext.PROXY_MD, ProxyConstantContext.PROXY_MD);
        insertContractItem(contractIndex++,proxyFolderId,"Address", ToolsConstantContext.ADDRESS_SOURCE,
                ProxyConstantContext.PROXY_MD, ProxyConstantContext.PROXY_MD);
    }


    public void insertContractItem(long contractId, long contractFolderId, String contractName, String contractSrc,
                                String contractDesc, String contractDesc_en)
    {
        log.info("insert contractItem id:{},contractName:{}", contractId, contractName);
        if (contractItemRepository.existsById(contractId)) {
            log.info("insertContractItem skip contractId:{} already exist", contractId);
            return;
        }
        ContractItem contractItem = new ContractItem();
        contractItem.setContractId(contractId);
        contractItem.setContractFolderId(contractFolderId);
        contractItem.setContractName(contractName);
        contractItem.setContractSrc(contractSrc);
        contractItem.setContractDesc(contractDesc);
        contractItem.setContractDesc_en(contractDesc_en);
        contractItem.setCreateTime(LocalDateTime.now());
        contractItem.setModifyTime(contractItem.getCreateTime());
        contractItemRepository.save(contractItem);
    }

    public void initPresetData()
    {
//        initContractItem();
//        initContractFolderItem();
//        initStoreItem();
        this.readAndInitContractItem();
        this.readAndInitFolderItem();
        this.readAndInitStoreItem();
    }

    /**
     * read from ./resources/warehouse/*.json
     */
    public void readAndInitStoreItem() {
        String jsonStr = this.loadWarehouseJson("warehouse/warehouse.json");
        List<StoreItem> storeItems = JsonUtils.toJavaObjectList(jsonStr, StoreItem.class);
        if (storeItems == null) {
            log.error("readAndInitStoreItem get null");
            return;
        }
        List<StoreItem> item2Save = new ArrayList<>();
        for (StoreItem item : storeItems) {
            if (!contractStoreRepository.existsById(item.getStoreId())) {
                item.setCreateTime(LocalDateTime.now());
                item.setModifyTime(item.getCreateTime());
                item2Save.add(item);
            }
        }
        contractStoreRepository.saveAll(item2Save);
        log.info("readAndInitStoreItem save {} items", storeItems.size());
    }

    public void readAndInitFolderItem() {
        String jsonStr = this.loadWarehouseJson("warehouse/folder.json");
        List<ContractFolderItem> folderItems = JsonUtils.toJavaObjectList(jsonStr, ContractFolderItem.class);
        if (folderItems == null) {
            log.error("readAndInitFolderItem get null");
            return;
        }
        List<ContractFolderItem> item2Save = new ArrayList<>();
        for (ContractFolderItem item : folderItems) {
            if (!contractFolderRepository.existsById(item.getContractFolderId())) {
                item.setCreateTime(LocalDateTime.now());
                item.setModifyTime(item.getCreateTime());
                item2Save.add(item);
            }
        }
        contractFolderRepository.saveAll(item2Save);
        log.info("readAndInitFolderItem save {} items", folderItems.size());
    }

    public void readAndInitContractItem() {
        String jsonStr = this.loadWarehouseJson("warehouse/contract.json");
        List<ContractItem> contractItems = JsonUtils.toJavaObjectList(jsonStr, ContractItem.class);
        if (contractItems == null) {
            log.error("readAndInitContractItem get null");
            return;
        }
        List<ContractItem> item2Save = new ArrayList<>();
        for (ContractItem item : contractItems) {
            if (!contractItemRepository.existsById(item.getContractId())) {
                item.setCreateTime(LocalDateTime.now());
                item.setModifyTime(item.getCreateTime());
                item2Save.add(item);
            }
        }
        contractItemRepository.saveAll(item2Save);
        log.info("readAndInitContractItem save {} items", contractItems.size());
    }

    private String loadWarehouseJson(String jsonFilePath) {
        log.info("loadWarehouseJson :{}", jsonFilePath);
        try (InputStream nodeCrtInput = new ClassPathResource(jsonFilePath).getInputStream()) {
            String jsonStr = IOUtils.toString(nodeCrtInput, StandardCharsets.UTF_8);
            log.debug("loadCrtContentByPath itemList:{}", jsonStr);
            return jsonStr;
        } catch (Exception e) {
            log.error("loadWarehouseJson error:[]", e);
            return null;
        }
    }
}
