package com.webank.webase.front.contractStore;

import com.webank.webase.front.contractStore.entity.ContractFolderItem;
import com.webank.webase.front.contractStore.entity.ContractItem;
import com.webank.webase.front.contractStore.entity.StoreItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PresetDataService {

    //todo 修改插入判断条件

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
            "Smart-Dev-Contracts仓库中的存证应用模板",
            "Smart-Dev-Contract\'s Evidence Contract suite of business_template",
            "Smart-Dev-Contracts仓库中的存证应用模板",
                    "Smart-Dev-Contract\'s Evidence Contract suite of business_template");
    }

    public void insertStoreItem(long storeId, String storeName, String StoreName_en, String StoreType,
                                String StoreIcon, String StoreDesc, String StoreDetail,String StoreDesc_en,
                                String StoreDetail_en)
    {
        if (contractStoreRepository.exists(storeId)) {
            log.info("insertStoreItem skip storeId:{} storeName:{} already exist", storeId, storeName);
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
        Integer contractFolderIndex = 1;
        insertContractFolderItem(contractFolderIndex++,toolboxId,"Tools",
                "工具箱中有常用的工具合约",
                "工具箱中有常用的工具合约",
                "Toolbox Contract suite",
                "Toolbox Contract suite");

        insertContractFolderItem(contractFolderIndex++,evidenceId,"Evidence",
                "一套区块链存证合约，实现区块链存证、取证",
                "一套区块链存证合约，实现区块链存证、取证",
                "Evidence Contract suite",
                "Evidence Contract suite");

        insertContractFolderItem(contractFolderIndex++,pointsId,"Points",
                "一套积分合约，具有积分相关的增发，销毁，暂停合约，黑白名单等权限控制等功能",
                "一套积分合约，具有积分相关的增发，销毁，暂停合约，黑白名单等权限控制等功能",
                "Points Contract suite",
                "Points Contract suite");

        insertContractFolderItem(contractFolderIndex++,toolboxId,"Smart_Dev_Evidence",
            "SmartDev基础合约，包含Table/KVTable/Sha/Crypto/HelloWorld等",
            "SmartDev基础合约，包含Table/KVTable/Sha/Crypto/HelloWorld等",
            "Smart-Dev-Contract basic contract suite, including Table/KVTable/Sha/Crypto/HelloWorld etc.",
            "Smart-Dev-Contract basic contract suite, including Table/KVTable/Sha/Crypto/HelloWorld etc.");

        insertContractFolderItem(contractFolderIndex++,smartDevId,"Smart_Dev_Evidence",
            "SmartDev存证合约案例",
            "SmartDev存证合约案例",
            "Smart-Dev-Contract Evidence contract suite",
            "Smart-Dev-Contract Evidence contract suite");
    }

    public void insertContractFolderItem(long contractFolderId, long storeId, String contractFolderName, String contractFolderDesc, String contractFolderDetail,
                                         String contractFolderDesc_en, String contractFolderDetail_en)
    {
        if (contractFolderRepository.exists(contractFolderId)) {
            log.info("insertContractFolderItem skip contractFolderId:{} contractFolderName:{} storeId:{} already exist",
                contractFolderId, contractFolderName, storeId);
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
        Integer contractIndex = 1;
        //tools
        insertContractItem(contractIndex++,toolboxId,"Address",ToolsConstantContext.ADDRESS_SOURCE,
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
        insertContractItem(contractIndex++,smartDevEvidenceFolderId,"EvidenceController",SmartDevConstantContext.EVI_CONTROLLER_SOURCE,
            SmartDevConstantContext.EVIDENCE_API_MD,SmartDevConstantContext.EVIDENCE_API_MD);
        insertContractItem(contractIndex++,smartDevEvidenceFolderId,"EvidenceRepository",SmartDevConstantContext.EVI_REPO_SOURCE,
            SmartDevConstantContext.EVIDENCE_API_MD,SmartDevConstantContext.EVIDENCE_API_MD);
        insertContractItem(contractIndex++,smartDevEvidenceFolderId,"RequestRepository",SmartDevConstantContext.REQUEST_REPO_SOURCE,
            SmartDevConstantContext.EVIDENCE_API_MD,SmartDevConstantContext.EVIDENCE_API_MD);
        insertContractItem(contractIndex++,smartDevEvidenceFolderId,"Authentication",SmartDevConstantContext.AUTH_SOURCE,
            SmartDevConstantContext.EVIDENCE_API_MD,SmartDevConstantContext.EVIDENCE_API_MD);
    }


    public void insertContractItem(long contractId, long contractFolderId, String contractName, String contractSrc,
                                String contractDesc, String contractDesc_en)
    {
        if (contractItemRepository.exists(contractId)) {
            log.info("insertContractItem skip contractId:{} contractName:{} contractFolderId:{} already exist",
                contractId, contractName, contractFolderId);
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
        initContractItem();
        initContractFolderItem();
        initStoreItem();
//        List<ContractItem> contracts = contractItemRepository.findAll();
//        if(contracts.size() <= 0)
//        {
//            initContractItem();
//        }
//
//        List<ContractFolderItem> contractFolderItemList = contractFolderRepository.findAll();
//        if(contractFolderItemList.size() <= 0)
//        {
//            initContractFolderItem();
//        }
//
//        List<StoreItem> storeItemList = contractStoreRepository.findAll();
//        if(storeItemList.size() <= 0)
//        {
//            initStoreItem();
//        }
    }

}
