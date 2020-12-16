package com.webank.webase.front.contractStore;

import com.webank.webase.front.contract.entity.Contract;
import com.webank.webase.front.contractStore.ContractFolderRepository;
import com.webank.webase.front.contractStore.ContractItemRepository;
import com.webank.webase.front.contractStore.ContractStoreRepository;
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


    public void initStoreItem() {
        insertStoreItem(1,"工具箱","Toolbox","1","1",
                "工具箱中有常用的工具合约","工具箱中有常用的工具合约",
                "There are common tool contracts in the toolbox","There are common tool contracts in the toolbox");

        insertStoreItem(2,"存证应用","Evidence","2","1",
                "一套区块链存证合约","一套区块链存证合约",
                "A set of contracts for the preservation of evidence","A set of contracts for the preservation of evidence");

        insertStoreItem(3,"存证应用","Evidence","2","1",
                "一套区块链存证合约","一套区块链存证合约",
                "A set of contracts for the preservation of evidence","A set of contracts for the preservation of evidence");
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
        insertContractFolderItem(1,1,"address","address",
                "address","","");

        insertContractFolderItem(2,2,"address","address",
                "address","","");

        insertContractFolderItem(3,3,"address","address",
                "address","","");
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

        insertContractItem(1,1,"LibString","LyoKICogQ29weXJpZ2h0IDIwMTQtMjAxOSB0aGUgb3JpZ2luYWwgYXV0aG9yIG9yIGF1dGhvcnMuCiAqCiAqIExpY2Vuc2VkIHVuZGVyIHRoZSBBcGFjaGUgTGljZW5zZSwgVmVyc2lvbiAyLjAgKHRoZSAiTGljZW5zZSIpOwogKiB5b3UgbWF5IG5vdCB1c2UgdGhpcyBmaWxlIGV4Y2VwdCBpbiBjb21wbGlhbmNlIHdpdGggdGhlIExpY2Vuc2UuCiAqIFlvdSBtYXkgb2J0YWluIGEgY29weSBvZiB0aGUgTGljZW5zZSBhdAogKgogKiAgICAgIGh0dHA6Ly93d3cuYXBhY2hlLm9yZy9saWNlbnNlcy9MSUNFTlNFLTIuMAogKgogKiBVbmxlc3MgcmVxdWlyZWQgYnkgYXBwbGljYWJsZSBsYXcgb3IgYWdyZWVkIHRvIGluIHdyaXRpbmcsIHNvZnR3YXJlCiAqIGRpc3RyaWJ1dGVkIHVuZGVyIHRoZSBMaWNlbnNlIGlzIGRpc3RyaWJ1dGVkIG9uIGFuICJBUyBJUyIgQkFTSVMsCiAqIFdJVEhPVVQgV0FSUkFOVElFUyBPUiBDT05ESVRJT05TIE9GIEFOWSBLSU5ELCBlaXRoZXIgZXhwcmVzcyBvciBpbXBsaWVkLgogKiBTZWUgdGhlIExpY2Vuc2UgZm9yIHRoZSBzcGVjaWZpYyBsYW5ndWFnZSBnb3Zlcm5pbmcgcGVybWlzc2lvbnMgYW5kCiAqIGxpbWl0YXRpb25zIHVuZGVyIHRoZSBMaWNlbnNlLgogKiAqLwoKcHJhZ21hIHNvbGlkaXR5IF4wLjQuMjU7CgpsaWJyYXJ5IExpYkFkZHJlc3N7CiAgICAKICAgIGZ1bmN0aW9uIGlzQ29udHJhY3QoYWRkcmVzcyBhY2NvdW50KSBpbnRlcm5hbCB2aWV3IHJldHVybnMoYm9vbCkgewogICAgICAgIHVpbnQyNTYgc2l6ZTsKICAgICAgICBhc3NlbWJseSB7IHNpemUgOj0gZXh0Y29kZXNpemUoYWRkcikgfSAgCiAgICAgICAgcmV0dXJuIHNpemUgPiAwOwogICAgfQoKICAgIGZ1bmN0aW9uIGlzRW1wdHlBZGRyZXNzKGFkZHJlc3MgYWRkcikgaW50ZXJuYWwgcHVyZSByZXR1cm5zKGJvb2wpewogICAgICAgIHJldHVybiBhZGRyID09IGFkZHJlc3MoMCk7CiAgICB9Cn0K",
                "LibString","LibString");
        insertContractItem(2,2,"LibString","LyoKICogQ29weXJpZ2h0IDIwMTQtMjAxOSB0aGUgb3JpZ2luYWwgYXV0aG9yIG9yIGF1dGhvcnMuCiAqCiAqIExpY2Vuc2VkIHVuZGVyIHRoZSBBcGFjaGUgTGljZW5zZSwgVmVyc2lvbiAyLjAgKHRoZSAiTGljZW5zZSIpOwogKiB5b3UgbWF5IG5vdCB1c2UgdGhpcyBmaWxlIGV4Y2VwdCBpbiBjb21wbGlhbmNlIHdpdGggdGhlIExpY2Vuc2UuCiAqIFlvdSBtYXkgb2J0YWluIGEgY29weSBvZiB0aGUgTGljZW5zZSBhdAogKgogKiAgICAgIGh0dHA6Ly93d3cuYXBhY2hlLm9yZy9saWNlbnNlcy9MSUNFTlNFLTIuMAogKgogKiBVbmxlc3MgcmVxdWlyZWQgYnkgYXBwbGljYWJsZSBsYXcgb3IgYWdyZWVkIHRvIGluIHdyaXRpbmcsIHNvZnR3YXJlCiAqIGRpc3RyaWJ1dGVkIHVuZGVyIHRoZSBMaWNlbnNlIGlzIGRpc3RyaWJ1dGVkIG9uIGFuICJBUyBJUyIgQkFTSVMsCiAqIFdJVEhPVVQgV0FSUkFOVElFUyBPUiBDT05ESVRJT05TIE9GIEFOWSBLSU5ELCBlaXRoZXIgZXhwcmVzcyBvciBpbXBsaWVkLgogKiBTZWUgdGhlIExpY2Vuc2UgZm9yIHRoZSBzcGVjaWZpYyBsYW5ndWFnZSBnb3Zlcm5pbmcgcGVybWlzc2lvbnMgYW5kCiAqIGxpbWl0YXRpb25zIHVuZGVyIHRoZSBMaWNlbnNlLgogKiAqLwoKcHJhZ21hIHNvbGlkaXR5IF4wLjQuMjU7CgpsaWJyYXJ5IExpYkFkZHJlc3N7CiAgICAKICAgIGZ1bmN0aW9uIGlzQ29udHJhY3QoYWRkcmVzcyBhY2NvdW50KSBpbnRlcm5hbCB2aWV3IHJldHVybnMoYm9vbCkgewogICAgICAgIHVpbnQyNTYgc2l6ZTsKICAgICAgICBhc3NlbWJseSB7IHNpemUgOj0gZXh0Y29kZXNpemUoYWRkcikgfSAgCiAgICAgICAgcmV0dXJuIHNpemUgPiAwOwogICAgfQoKICAgIGZ1bmN0aW9uIGlzRW1wdHlBZGRyZXNzKGFkZHJlc3MgYWRkcikgaW50ZXJuYWwgcHVyZSByZXR1cm5zKGJvb2wpewogICAgICAgIHJldHVybiBhZGRyID09IGFkZHJlc3MoMCk7CiAgICB9Cn0K",
                "LibString","LibString");
        insertContractItem(3,3,"LibString","LyoKICogQ29weXJpZ2h0IDIwMTQtMjAxOSB0aGUgb3JpZ2luYWwgYXV0aG9yIG9yIGF1dGhvcnMuCiAqCiAqIExpY2Vuc2VkIHVuZGVyIHRoZSBBcGFjaGUgTGljZW5zZSwgVmVyc2lvbiAyLjAgKHRoZSAiTGljZW5zZSIpOwogKiB5b3UgbWF5IG5vdCB1c2UgdGhpcyBmaWxlIGV4Y2VwdCBpbiBjb21wbGlhbmNlIHdpdGggdGhlIExpY2Vuc2UuCiAqIFlvdSBtYXkgb2J0YWluIGEgY29weSBvZiB0aGUgTGljZW5zZSBhdAogKgogKiAgICAgIGh0dHA6Ly93d3cuYXBhY2hlLm9yZy9saWNlbnNlcy9MSUNFTlNFLTIuMAogKgogKiBVbmxlc3MgcmVxdWlyZWQgYnkgYXBwbGljYWJsZSBsYXcgb3IgYWdyZWVkIHRvIGluIHdyaXRpbmcsIHNvZnR3YXJlCiAqIGRpc3RyaWJ1dGVkIHVuZGVyIHRoZSBMaWNlbnNlIGlzIGRpc3RyaWJ1dGVkIG9uIGFuICJBUyBJUyIgQkFTSVMsCiAqIFdJVEhPVVQgV0FSUkFOVElFUyBPUiBDT05ESVRJT05TIE9GIEFOWSBLSU5ELCBlaXRoZXIgZXhwcmVzcyBvciBpbXBsaWVkLgogKiBTZWUgdGhlIExpY2Vuc2UgZm9yIHRoZSBzcGVjaWZpYyBsYW5ndWFnZSBnb3Zlcm5pbmcgcGVybWlzc2lvbnMgYW5kCiAqIGxpbWl0YXRpb25zIHVuZGVyIHRoZSBMaWNlbnNlLgogKiAqLwoKcHJhZ21hIHNvbGlkaXR5IF4wLjQuMjU7CgpsaWJyYXJ5IExpYkFkZHJlc3N7CiAgICAKICAgIGZ1bmN0aW9uIGlzQ29udHJhY3QoYWRkcmVzcyBhY2NvdW50KSBpbnRlcm5hbCB2aWV3IHJldHVybnMoYm9vbCkgewogICAgICAgIHVpbnQyNTYgc2l6ZTsKICAgICAgICBhc3NlbWJseSB7IHNpemUgOj0gZXh0Y29kZXNpemUoYWRkcikgfSAgCiAgICAgICAgcmV0dXJuIHNpemUgPiAwOwogICAgfQoKICAgIGZ1bmN0aW9uIGlzRW1wdHlBZGRyZXNzKGFkZHJlc3MgYWRkcikgaW50ZXJuYWwgcHVyZSByZXR1cm5zKGJvb2wpewogICAgICAgIHJldHVybiBhZGRyID09IGFkZHJlc3MoMCk7CiAgICB9Cn0K",
                "LibString","LibString");
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
