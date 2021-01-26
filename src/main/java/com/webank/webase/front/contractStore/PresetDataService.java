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

    @Autowired
    private ContractItemRepository contractItemRepository;
    @Autowired
    private ContractFolderRepository contractFolderRepository;
    @Autowired
    private ContractStoreRepository contractStoreRepository;

    public static final Integer toolboxId = 1;
    public static final Integer evidenceId = 2;
    public static final Integer pointsId = 3;


    public void initStoreItem() {
        insertStoreItem(toolboxId,"工具箱","Toolbox","1","toolboxId",
                "工具箱中有常用的工具合约","工具箱中有常用的工具合约",
                "Toolbox Contract suite","Toolbox Contract suite");

        insertStoreItem(evidenceId,"存证应用","Evidence","2","evidenceId",
                "一套区块链存证合约，实现区块链存证、取证",
                "一套区块链存证合约，实现区块链存证、取证",
                "Evidence Contract suite","Evidence Contract suite");

        insertStoreItem(pointsId,"积分应用","Points","3","pointsId",
                "一套积分合约，具有积分相关的增发，销毁，暂停合约，黑白名单等权限控制等功能",
                "一套积分合约，具有积分相关的增发，销毁，暂停合约，黑白名单等权限控制等功能",
                "Points Contract suite","Points Contract suite");
    }

    public void insertStoreItem(long storeId, String storeName, String StoreName_en, String StoreType,
                                String StoreIcon, String StoreDesc, String StoreDetail,String StoreDesc_en,
                                String StoreDetail_en)
    {
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
    }

    public void insertContractFolderItem(long contractFolderId, long storeId, String contractFolderName, String contractFolderDesc, String contractFolderDetail,
                                         String contractFolderDesc_en, String contractFolderDetail_en)
    {
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
    }


    public void insertContractItem(long contractId, long contractFolderId, String contractName, String contractSrc,
                                String contractDesc, String contractDesc_en)
    {
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
        List<ContractItem> contracts = contractItemRepository.findAll();
        if(contracts.size() <= 0)
        {
            initContractItem();
        }

        List<ContractFolderItem> contractFolderItemList = contractFolderRepository.findAll();
        if(contractFolderItemList.size() <= 0)
        {
            initContractFolderItem();
        }

        List<StoreItem> storeItemList = contractStoreRepository.findAll();
        if(storeItemList.size() <= 0)
        {
            initStoreItem();
        }
    }

}
